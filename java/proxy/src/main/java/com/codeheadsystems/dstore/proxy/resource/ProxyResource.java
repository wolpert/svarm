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

package com.codeheadsystems.dstore.proxy.resource;

import static org.slf4j.LoggerFactory.getLogger;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.codeheadsystems.dstore.proxy.common.api.ProxyService;
import com.codeheadsystems.dstore.proxy.manager.TableEntryManager;
import com.codeheadsystems.server.resource.JerseyResource;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

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
    return tableEntryManager.getTenantTableEntry(tenantId, table, entry);
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
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public void deleteTenantTableEntry(final String tenantId,
                                     final String table,
                                     final String entry) {
    LOGGER.trace("readTenantTableEntry({},{},{})", tenantId, table, entry);
  }
}
