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

package org.svarm.proxy.resource;

import static org.slf4j.LoggerFactory.getLogger;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.api.ImmutableTenantResource;
import org.svarm.common.config.api.TenantResource;
import org.svarm.proxy.common.api.ProxyService;
import org.svarm.proxy.manager.TableEntryManager;
import org.svarm.server.resource.JerseyResource;

/**
 * Main proxy resource.
 */
@Singleton
public class ProxyResource implements ProxyService, JerseyResource {

  private static final Logger LOGGER = getLogger(ProxyResource.class);

  private final TableEntryManager tableEntryManager;


  /**
   * Constructor.
   *
   * @param tableEntryManager to get the real data.
   */
  @Inject
  public ProxyResource(final TableEntryManager tableEntryManager) {
    this.tableEntryManager = tableEntryManager;
    LOGGER.info("ProxyResource()");
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public Optional<JsonNode> readTenantTableEntry(final String tenantId,
                                                 final String table,
                                                 final String entry) {
    LOGGER.trace("readTenantTableEntry({},{},{})", tenantId, table, entry);
    final TenantResource tenantResource = ImmutableTenantResource.builder()
        .tenant(tenantId).resource(table).build();
    return tableEntryManager.getTenantTableEntry(tenantResource, entry);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public void createTenantTableEntry(final String tenantId,
                                     final String table,
                                     final String entry,
                                     final JsonNode data) {
    LOGGER.trace("createTenantTableEntry({},{},{})", tenantId, table, entry);
    final TenantResource tenantResource = ImmutableTenantResource.builder()
        .tenant(tenantId).resource(table).build();
    tableEntryManager.putTenantTableEntry(tenantResource, entry, data);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public void deleteTenantTableEntry(final String tenantId,
                                     final String table,
                                     final String entry) {
    LOGGER.trace("deleteTenantTableEntry({},{},{})", tenantId, table, entry);
    final TenantResource tenantResource = ImmutableTenantResource.builder()
        .tenant(tenantId).resource(table).build();
    tableEntryManager.deleteTenantTableEntry(tenantResource, entry);
  }
}
