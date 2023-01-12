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

import com.codeheadsystems.dstore.common.crypt.AesGcmSivManager;
import com.codeheadsystems.dstore.node.dao.TenantTableDao;
import com.codeheadsystems.dstore.node.engine.TableDefinitionEngine;
import com.codeheadsystems.dstore.node.model.ImmutableTenantTable;
import com.codeheadsystems.dstore.node.model.ImmutableTenantTableIdentifier;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.codeheadsystems.metrics.Metrics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages tenant tables.
 */
@Singleton
public class TenantTableManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableManager.class);

  private final Metrics metrics;
  private final TenantTableDao dao;
  private final AesGcmSivManager aesGcmSivManager;
  private final Map<String, TableDefinitionEngine> tableDefinitionEngineMap;

  /**
   * Default constructor.
   *
   * @param metrics                  to use.
   * @param dao                      to use.
   * @param aesGcmSivManager         to crypt controls.
   * @param tableDefinitionEngineMap map of available engines.
   */
  @Inject
  public TenantTableManager(final Metrics metrics,
                            final TenantTableDao dao,
                            final AesGcmSivManager aesGcmSivManager,
                            final Map<String, TableDefinitionEngine> tableDefinitionEngineMap) {
    LOGGER.info("TenantManager({},{},{},{})", metrics, dao, aesGcmSivManager, tableDefinitionEngineMap);
    this.metrics = metrics;
    this.dao = dao;
    this.aesGcmSivManager = aesGcmSivManager;
    this.tableDefinitionEngineMap = tableDefinitionEngineMap;
  }

  /**
   * Gets the current tenant table if it exists.
   *
   * @param tenantId  to get.
   * @param tableName to get.
   * @return the tenant.
   */
  public Optional<TenantTable> get(final String tenantId, final String tableName) {
    LOGGER.debug("get({}, {})", tenantId, tableName);
    return metrics.time("TenantTableManager.get", () -> dao.read(tenantId, tableName));
  }

  /**
   * Created the tenant table. If it already exists, simply return the one we already have. Idempotent. Does not set the
   * hash values.
   *
   * @param tenantId     tenant to create.
   * @param tableName    table name to create.
   * @param tableVersion the version of the table we are creating.
   * @return a tenant.
   */
  public TenantTable create(final String tenantId, final String tableName, final String tableVersion) {
    LOGGER.debug("create({}, {}, {})", tenantId, tableName, tableVersion);
    return get(tenantId, tableName).orElseGet(() ->
        metrics.time("TenantTableManager.create",
            () -> dao.create(buildTenantTable(tenantId, tableName, tableVersion))));
  }

  private TenantTable buildTenantTable(final String tenantId, final String tableName, final String tableVersion) {
    LOGGER.debug("buildTenantTable({}, {}, {})", tenantId, tableName, tableVersion);
    if (!tableDefinitionEngineMap.containsKey(tableVersion)) {
      throw new IllegalArgumentException("Unknown table version: " + tableVersion);
    }
    final TenantTableIdentifier identifier = ImmutableTenantTableIdentifier.builder()
        .tenantId(tenantId)
        .tableName(tableName)
        .build();
    return ImmutableTenantTable.builder()
        .identifier(identifier)
        .enabled(true)
        .estimatedQuantity(0)
        .tableVersion(tableVersion)
        .key(aesGcmSivManager.randomKeyBase64Encoded())
        .nonce(aesGcmSivManager.randomNonceBase64Encoded())
        .build();
  }

  /**
   * Lists current tables for the tenant.
   *
   * @param tenantId tenant id to look up.
   * @return a list of tables.
   */
  public List<String> tables(final String tenantId) {
    LOGGER.debug("tables({})", tenantId);
    return metrics.time("TenantTableManager.tenants", () -> dao.allTenantTables(tenantId));
  }

  /**
   * Deletes the table. This will destroy the table as well.
   * TODO: delete the actual table.
   *
   * @param tenantId  to delete.
   * @param tableName tableName to delete.
   * @return boolean if deleted or not.
   */
  public boolean delete(final String tenantId, final String tableName) {
    LOGGER.debug("delete({}, {})", tenantId, tableName);
    return metrics.time("TenantTableManager.delete", () -> dao.delete(tenantId, tableName));
  }

}
