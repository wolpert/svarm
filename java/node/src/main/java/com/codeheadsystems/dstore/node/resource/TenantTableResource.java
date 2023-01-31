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
import com.codeheadsystems.dstore.node.api.NodeTenantTableService;
import com.codeheadsystems.dstore.node.api.TenantTableInfo;
import com.codeheadsystems.dstore.node.converter.TenantTableInfoConverter;
import com.codeheadsystems.dstore.node.engine.impl.V1SingleEntryEngine;
import com.codeheadsystems.dstore.node.manager.TenantTableManager;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.codeheadsystems.server.exception.NotFoundException;
import com.codeheadsystems.server.resource.JerseyResource;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                                           final String table) {
    LOGGER.debug("create({},{})", tenantId, table);
    final TenantTableIdentifier identifier = TenantTableIdentifier.from(tenantId, table);
    final TenantTable tenantTable = tenantTableManager
        .create(identifier, V1SingleEntryEngine.DEFINITION_NAME);
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
