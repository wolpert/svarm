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

package com.codeheadsystems.dstore.node.manager;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.common.config.api.NodeTenantResourceRange;
import com.codeheadsystems.dstore.common.config.api.TenantResource;
import com.codeheadsystems.dstore.common.config.converter.NodeTenantResourceRangeConverter;
import com.codeheadsystems.dstore.common.config.engine.Event;
import com.codeheadsystems.dstore.common.config.engine.WatchEngine;
import com.codeheadsystems.dstore.common.config.factory.WatchEngineFactory;
import com.codeheadsystems.dstore.node.engine.impl.V1SingleEntryEngine;
import com.codeheadsystems.dstore.node.model.ImmutableTenantTableIdentifier;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.lifecycle.Managed;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * This class manages the control plane watcher. Which is how we get directions of what to do.
 */
@Singleton
public class ControlPlaneWatcherManager implements Managed {

  private static final Logger LOGGER = getLogger(ControlPlaneWatcherManager.class);

  private final String uuid;
  private final WatchEngineFactory watchEngineFactory;
  private final TenantTableManager tenantTableManager;
  private WatchEngine engine;
  private NodeTenantResourceRangeConverter nodeTenantResourceRangeConverter;

  /**
   * Constructor.
   *
   * @param configuration                    to get the uuid.
   * @param factory                          to generate the engine.
   * @param tenantTableManager               to create tenant tables.
   * @param nodeTenantResourceRangeConverter to convert into tenant tables.
   */
  @Inject
  public ControlPlaneWatcherManager(final NodeInternalConfiguration configuration,
                                    final WatchEngineFactory factory,
                                    final TenantTableManager tenantTableManager,
                                    final NodeTenantResourceRangeConverter nodeTenantResourceRangeConverter) {
    this.tenantTableManager = tenantTableManager;
    this.watchEngineFactory = factory;
    this.uuid = configuration.uuid();
    this.nodeTenantResourceRangeConverter = nodeTenantResourceRangeConverter;
    LOGGER.info("ControlPlaneWatcherManager({},{})", factory, tenantTableManager);
  }

  @VisibleForTesting
  void handle(final Event event) {
    LOGGER.trace("handle({})", event);
    switch (event.type()) {
      case PUT -> handleNewTable(event.key(), event.value()
          .orElseThrow(() -> new IllegalArgumentException("No value for event " + event)));
      case DELETE -> LOGGER.warn("We cannot handle delete yet! " + event);
      default -> LOGGER.error("We never even heard of this one." + event);
    }
  }

  private void handleNewTable(final String key, final String value) {
    LOGGER.info("handleNewTable({},{})", key, value);
    final NodeTenantResourceRange range = nodeTenantResourceRangeConverter.fromKeyValue(key, value);
    final TenantResource tenantResource = range.nodeTenantResource().tenantResource();
    final TenantTableIdentifier identifier = ImmutableTenantTableIdentifier.builder()
        .tenantId(tenantResource.tenant()).tableName(tenantResource.resource()).build();
    final TenantTable tenantTable = tenantTableManager.create(identifier, V1SingleEntryEngine.DEFINITION_NAME);
    LOGGER.info("handleNewTable({},{}) : {}", key, value, tenantTable);
  }

  @Override
  public void start() throws Exception {
    if (engine == null) {
      LOGGER.info("start()");
      engine = watchEngineFactory.watchEngine("node", uuid, this::handle);
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
