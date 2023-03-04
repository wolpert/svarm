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
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.api.NodeTenantTableService;
import org.svarm.node.api.TableMetaData;
import org.svarm.node.api.TenantTableInfo;
import org.svarm.node.converter.TenantTableInfoConverter;
import org.svarm.node.engine.impl.V1SingleEntryEngine;
import org.svarm.node.manager.TenantTableManager;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;
import org.svarm.server.exception.NotFoundException;
import org.svarm.server.resource.JerseyResource;

/**
 * Resource for the tenant requests. (Control plane)
 */
@Singleton
public class TenantTableResource implements NodeTenantTableService, JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableResource.class);

  private final TenantTableManager tenantTableManager;
  private final TenantTableInfoConverter converter;

  /**
   * Default constructor.
   *
   * @param tenantTableManager to manage the tenant table.
   * @param converter          converter.
   */
  @Inject
  public TenantTableResource(final TenantTableManager tenantTableManager,
                             final TenantTableInfoConverter converter) {
    LOGGER.info("TenantResource({}, {})", tenantTableManager, converter);
    this.tenantTableManager = tenantTableManager;
    this.converter = converter;
  }

  /**
   * Lists the tables for the tenant we have.
   *
   * @param tenantId the tenant to search for.
   * @return response.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public List<String> listTenantTables(final String tenantId) {
    LOGGER.debug("list({})", tenantId);
    return tenantTableManager.tables(tenantId);
  }

  /**
   * Gets the tenant.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @return response.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public Optional<TenantTableInfo> readTenantTable(final String tenantId,
                                                   final String table) {
    LOGGER.debug("read({},{})", tenantId, table);
    final TenantTableIdentifier identifier = TenantTableIdentifier.from(tenantId, table);
    return tenantTableManager.get(identifier)
        .map(TenantTable::identifier)
        .map(converter::from);
  }

  /**
   * Create the tenant.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @return response.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public TenantTableInfo createTenantTable(final String tenantId,
                                           final String table,
                                           final TableMetaData tableMetaData) {
    LOGGER.debug("create({},{})", tenantId, table);
    final TenantTableIdentifier identifier = TenantTableIdentifier.from(tenantId, table);
    final TenantTable tenantTable = tenantTableManager
        .create(identifier, TableDefinition.V1SingleEntryEngine);
    return converter.from(tenantTable.identifier());
  }


  /**
   * Delete the tenant table.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public void deleteTenantTable(final String tenantId,
                                final String table) {
    LOGGER.debug("delete({},{})", tenantId, table);
    final TenantTableIdentifier identifier = TenantTableIdentifier.from(tenantId, table);
    if (!tenantTableManager.delete(identifier)) {
      LOGGER.debug("not found {}:{}", tenantId, table);
      throw new NotFoundException();
    }
  }

}
