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

package org.svarm.node.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.engine.HashingEngine;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.api.ImmutableEntryInfo;
import org.svarm.node.api.NodeTenantTableEntryService;
import org.svarm.node.manager.TenantTableEntryManager;
import org.svarm.node.model.TenantTableIdentifier;
import org.svarm.server.exception.NotFoundException;
import org.svarm.server.resource.JerseyResource;

/**
 * Resource for the tenant requests. (Control plane)
 */
@Singleton
public class TenantTableEntryResource implements NodeTenantTableEntryService, JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableEntryResource.class);

  private final TenantTableEntryManager tenantTableEntryManager;
  private final HashingEngine hashingEngine;

  /**
   * Default constructor.
   *
   * @param tenantTableEntryManager to manage the tenant table.
   * @param hashingEngine           for hashing. Temporary.
   */
  @Inject
  public TenantTableEntryResource(final TenantTableEntryManager tenantTableEntryManager,
                                  final HashingEngine hashingEngine) {
    this.hashingEngine = hashingEngine;
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
    return tenantTableEntryManager.read(TenantTableIdentifier.from(tenantId, table), entry)
        .map(EntryInfo::data);
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
                                     final JsonNode data) { // Do NOT log this data!
    LOGGER.debug("write({},{},{})", tenantId, table, entry);
    final EntryInfo entryInfo = ImmutableEntryInfo.builder()
        .id(entry)
        .data(data)
        .timestamp(System.currentTimeMillis())
        .locationHash(hashingEngine.murmur3(entry))// temporary.
        .build();
    tenantTableEntryManager.write(TenantTableIdentifier.from(tenantId, table), entryInfo);
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
