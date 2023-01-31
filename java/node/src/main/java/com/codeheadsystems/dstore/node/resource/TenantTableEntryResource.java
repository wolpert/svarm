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

package com.codeheadsystems.dstore.node.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.codeheadsystems.dstore.node.api.NodeTenantTableEntryService;
import com.codeheadsystems.dstore.node.manager.TenantTableEntryManager;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.codeheadsystems.server.exception.NotFoundException;
import com.codeheadsystems.server.resource.JerseyResource;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource for the tenant requests. (Control plane)
 */
@Singleton
public class TenantTableEntryResource implements NodeTenantTableEntryService, JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableEntryResource.class);

  private final TenantTableEntryManager tenantTableEntryManager;

  /**
   * Default constructor.
   *
   * @param tenantTableEntryManager to manage the tenant table.
   */
  @Inject
  public TenantTableEntryResource(final TenantTableEntryManager tenantTableEntryManager) {
    LOGGER.info("TenantTableEntryResource({})", tenantTableEntryManager);
    this.tenantTableEntryManager = tenantTableEntryManager;
  }

  /**
   * Gets the entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    the entry.
   * @return response.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public Optional<JsonNode> readTenantTableEntry(final String tenantId,
                                                 final String table,
                                                 final String entry) {
    LOGGER.debug("readTenantTableEntry({},{},{})", tenantId, table, entry);
    return tenantTableEntryManager.read(TenantTableIdentifier.from(tenantId, table), entry);
  }

  /**
   * Create the tenant table entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    for the table.
   * @param data     the data we care about in JSON form.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public void createTenantTableEntry(final String tenantId,
                                     final String table,
                                     final String entry,
                                     JsonNode data) { // Do NOT log this data!
    LOGGER.debug("write({},{},{})", tenantId, table, entry);
    tenantTableEntryManager.write(TenantTableIdentifier.from(tenantId, table), entry, data);
  }

  /**
   * Delete the tenant table entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    to delete.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public void deleteTenantTableEntry(final String tenantId,
                                     final String table,
                                     final String entry) {
    LOGGER.debug("delete({},{},{})", tenantId, table, entry);
    if (!tenantTableEntryManager.delete(TenantTableIdentifier.from(tenantId, table), entry)) {
      throw new NotFoundException();
    }
  }

}
