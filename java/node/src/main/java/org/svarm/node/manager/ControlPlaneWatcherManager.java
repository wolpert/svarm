/*
 * Copyright (c) 2023. Ned Wolpert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.svarm.node.manager;

import static org.slf4j.LoggerFactory.getLogger;
import static org.svarm.common.config.api.MetaData.ACTION_DELETE;
import static org.svarm.common.config.api.MetaData.ACTION_REBALANCE;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.lifecycle.Managed;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.api.NodeTenantResourceRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.converter.NodeTenantResourceRangeConverter;
import org.svarm.common.config.engine.Event;
import org.svarm.common.config.engine.WatchEngine;
import org.svarm.common.config.factory.WatchEngineFactory;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.model.ImmutableTenantTableIdentifier;
import org.svarm.node.model.NodeInternalConfiguration;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * This class manages the control plane watcher. Which is how we get directions of what to do.
 */
@Singleton
public class ControlPlaneWatcherManager implements Managed {

  private static final Logger LOGGER = getLogger(ControlPlaneWatcherManager.class);

  private final String uuid;
  private final WatchEngineFactory watchEngineFactory;
  private final TenantTableManager tenantTableManager;
  private final NodeTenantResourceRangeConverter nodeTenantResourceRangeConverter;
  private final ControlPlaneManager controlPlaneManager;
  private volatile WatchEngine engine;

  /**
   * Constructor.
   *
   * @param configuration                    to get the uuid.
   * @param factory                          to generate the engine.
   * @param tenantTableManager               to create tenant tables.
   * @param nodeTenantResourceRangeConverter to convert into tenant tables.
   * @param controlPlaneManager              for enablement.
   */
  @Inject
  public ControlPlaneWatcherManager(final NodeInternalConfiguration configuration,
                                    final WatchEngineFactory factory,
                                    final TenantTableManager tenantTableManager,
                                    final NodeTenantResourceRangeConverter nodeTenantResourceRangeConverter,
                                    final ControlPlaneManager controlPlaneManager) {
    this.tenantTableManager = tenantTableManager;
    this.watchEngineFactory = factory;
    this.uuid = configuration.uuid();
    this.nodeTenantResourceRangeConverter = nodeTenantResourceRangeConverter;
    this.controlPlaneManager = controlPlaneManager;
    LOGGER.info("ControlPlaneWatcherManager({},{})", factory, tenantTableManager);
  }

  /**
   * Handle.
   *
   * @param event the event
   */
  @VisibleForTesting
  void handle(final Event event) {
    LOGGER.trace("handle({})", event);
    switch (event.type()) {
      case PUT -> handlePutEvent(event.key(), event.value()
          .orElseThrow(() -> new IllegalArgumentException("No value for event " + event)));
      // TODO: Metric failure on throw pls.
      case DELETE -> LOGGER.warn("We cannot handle delete yet! " + event);
      default -> LOGGER.error("We never even heard of this one." + event);
    }
  }

  private void handlePutEvent(final String key, final String value) {
    LOGGER.trace("handlePutEvent({},{})", key, value);
    final NodeTenantResourceRange range = nodeTenantResourceRangeConverter.fromKeyValue(key, value);
    range.metaData().action().ifPresentOrElse(action -> {
      switch (action) {
        case (ACTION_DELETE) -> handleDelete(range);
        case (ACTION_REBALANCE) -> LOGGER.warn("REBALACING not implemented."); // TODO: Implement.
        default -> LOGGER.warn("Not configured to handle other actions: {} : {}", action, range);
        // TODO: Metric failure pls.
      }
    }, () -> handleNewTable(range));
  }

  private void handleDelete(final NodeTenantResourceRange range) {
    LOGGER.info("handleDelete({})", range);
    final TenantResource tenantResource = range.nodeTenantResource().tenantResource();
    final TenantTableIdentifier identifier = ImmutableTenantTableIdentifier.builder()
        .tenantId(tenantResource.tenant()).tableName(tenantResource.resource()).build();
    if (tenantTableManager.delete(identifier)) {
      LOGGER.info("Deleted: {}", identifier);
    } else {
      LOGGER.info("Not found to delete: {}", identifier);
    }
    controlPlaneManager.delete(identifier); // self healing
  }

  private void handleNewTable(final NodeTenantResourceRange range) {
    LOGGER.info("handleNewTable({})", range);
    final TenantResource tenantResource = range.nodeTenantResource().tenantResource();
    final TenantTableIdentifier identifier = ImmutableTenantTableIdentifier.builder()
        .tenantId(tenantResource.tenant()).tableName(tenantResource.resource()).build();
    // TODO: Get this table definition from the request!
    final TenantTable tenantTable = tenantTableManager.create(identifier, TableDefinition.V1SingleEntryEngine);
    controlPlaneManager.enable(identifier);
    LOGGER.info("handleNewTable({}) : {}", range, tenantTable);
  }

  @Override
  public void start() throws Exception {
    if (engine == null) {
      LOGGER.info("start()");
      engine = watchEngineFactory.watchEngine("node", uuid, this::handle);
      // TODO: after we start watching, read what we have and handle those entries one by one.
    } else {
      LOGGER.warn("start(): already have an engine");
    }
  }

  @Override
  public void stop() throws Exception {
    if (engine != null) {
      LOGGER.info("stop(): closing");
      engine.close();
      engine = null;
    } else {
      LOGGER.warn("stop(): No engine");
    }
  }
}
