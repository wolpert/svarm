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

package com.codeheadsystems.dstore.proxy.manager;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.common.config.engine.NodeConfigurationEngine;
import com.codeheadsystems.dstore.common.config.factory.WatchEngineFactory;
import com.codeheadsystems.dstore.proxy.engine.NodeTenantTableEntryServiceEngine;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Handles the requests to the various nodes for a single entry.
 */
@Singleton
public class TableEntryManager {

  private static final Logger LOGGER = getLogger(TableEntryManager.class);

  private final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine;
  private final WatchEngineFactory watchEngineFactory;
  private final NodeConfigurationEngine nodeConfigurationEngine;


  /**
   * Constructor.
   *
   * @param nodeTenantTableEntryServiceEngine to get the node connections.
   * @param watchEngineFactory                to watch etcd.
   * @param nodeConfigurationEngine           to get node configuration data.
   */
  @Inject
  public TableEntryManager(final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine,
                           final WatchEngineFactory watchEngineFactory,
                           final NodeConfigurationEngine nodeConfigurationEngine) {
    this.nodeTenantTableEntryServiceEngine = nodeTenantTableEntryServiceEngine;
    this.watchEngineFactory = watchEngineFactory;
    this.nodeConfigurationEngine = nodeConfigurationEngine;
    LOGGER.info("TableEntryManager()");
  }

  /**
   * Provide a way to get the entry from the data stores.
   *
   * @param tenantId who it belongs to.
   * @param table    the table.
   * @param entry    the actual entry.
   * @return the value.
   */
  public Optional<JsonNode> getTenantTableEntry(final String tenantId,
                                                final String table,
                                                final String entry) {
    LOGGER.info("getTenantTableEntry({},{},{})", tenantId, table, entry);
    // get the node lists from etcd.
    // fan out the reads
    // collect the data and return.
    return Optional.empty();
  }
}
