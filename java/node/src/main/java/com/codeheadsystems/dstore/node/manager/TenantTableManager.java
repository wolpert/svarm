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
import com.codeheadsystems.dstore.node.exception.NotFoundException;
import com.codeheadsystems.dstore.node.model.ImmutableTenantTable;
import com.codeheadsystems.dstore.node.model.ImmutableTenantTableIdentifier;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.codeheadsystems.metrics.Metrics;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
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
  private final LoadingCache<TenantTableIdentifier, TenantTable> tenantTableCacheLoader;
  private final DataSourceManager dataSourceManager;

  /**
   * Default constructor.
   *
   * @param metrics                  to use.
   * @param dao                      to use.
   * @param aesGcmSivManager         to crypt controls.
   * @param tableDefinitionEngineMap map of available engines.
   * @param dataSourceManager        to ensure the data source exists.
   */
  @Inject
  public TenantTableManager(final Metrics metrics,
                            final TenantTableDao dao,
                            final AesGcmSivManager aesGcmSivManager,
                            final Map<String, TableDefinitionEngine> tableDefinitionEngineMap,
                            final DataSourceManager dataSourceManager) {
    LOGGER.info("TenantManager({},{},{},{})", metrics, dao, aesGcmSivManager, tableDefinitionEngineMap);
    this.dataSourceManager = dataSourceManager;
    this.metrics = metrics;
    this.dao = dao;
    this.aesGcmSivManager = aesGcmSivManager;
    this.tableDefinitionEngineMap = tableDefinitionEngineMap;
    tenantTableCacheLoader = CacheBuilder.newBuilder()
        .maximumSize(100)
        .build(CacheLoader.from(this::load));
  }

  /**
   * Gets the current tenant table if it exists.
   *
   * @param tenantId  to get.
   * @param tableName to get.
   * @return the tenant.
   */
  public Optional<TenantTable> get(final String tenantId, final String tableName) {
    LOGGER.trace("get({}, {})", tenantId, tableName);
    final TenantTableIdentifier identifier = TenantTableIdentifier.from(tenantId, tableName);
    return get(identifier);
  }


  /**
   * Gets the current tenant table if it exists.
   *
   * @param identifier to get.
   * @return the tenant.
   */
  public Optional<TenantTable> get(final TenantTableIdentifier identifier) {
    try {
      return Optional.of(tenantTableCacheLoader.get(identifier));
    } catch (NotFoundException nfe) {
      LOGGER.trace("Tenant not found (unchecked): {}", identifier);
      return Optional.empty();
    } catch (ExecutionException | UncheckedExecutionException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof NotFoundException) {
        LOGGER.trace("Tenant not found (checked): {}", identifier);
        return Optional.empty();
      } else {
        LOGGER.error("Loading tenant failed", cause);
        throw new RuntimeException(e);
      }
    }
  }

  private TenantTable load(final TenantTableIdentifier identifier) {
    return metrics.time("TenantTableManager.load", () -> dao.read(identifier.tenantId(), identifier.tableName()))
        .orElseThrow(() -> new NotFoundException("No such tenant table: " + identifier));
  }

  /**
   * Created the tenant table. If it already exists, simply return the one we already have. Idempotent. Does not set the
   * hash values.
   *
   * @param tenantId     tenant to create.
   * @param tableName    table name to create.
   * @param tableVersion the version of the table we are creating.
   * @param primaryKey   primary key of a row.
   * @return a tenant.
   */
  public TenantTable create(final String tenantId,
                            final String tableName,
                            final String tableVersion,
                            final String primaryKey) {
    LOGGER.debug("create({}, {}, {}, {})", tenantId, tableName, tableVersion, primaryKey);
    return get(tenantId, tableName).orElseGet(() ->
        metrics.time("TenantTableManager.create",
            () -> buildTenantTable(tenantId, tableName, tableVersion, primaryKey)));
  }

  private TenantTable buildTenantTable(final String tenantId,
                                       final String tableName,
                                       final String tableVersion,
                                       final String primaryKey) {
    LOGGER.debug("buildTenantTable({}, {}, {})", tenantId, tableName, tableVersion);
    if (!tableDefinitionEngineMap.containsKey(tableVersion)) {
      throw new IllegalArgumentException("Unknown table version: " + tableVersion);
    }
    final TenantTableIdentifier identifier = ImmutableTenantTableIdentifier.builder()
        .tenantId(tenantId)
        .tableName(tableName)
        .build();
    final TenantTable tenantTable = ImmutableTenantTable.builder()
        .identifier(identifier)
        .enabled(true)
        .estimatedQuantity(0)
        .tableVersion(tableVersion)
        .key(aesGcmSivManager.randomKeyBase64Encoded())
        .nonce(aesGcmSivManager.randomNonceBase64Encoded())
        .primaryKey(primaryKey)
        .build();
    try {
      final TenantTable result = dao.create(tenantTable);
      dataSourceManager.getDataSource(tenantTable);
      return result;
    } catch (RuntimeException re) {
      LOGGER.error("Unable to create data source for {}, destroying", tenantTable);
      dao.delete(tenantId, tableName);
      throw re;
    }
  }

  /**
   * Lists current tables for the tenant.
   *
   * @param tenantId tenant id to look up.
   * @return a list of tables.
   */
  public List<String> tables(final String tenantId) {
    LOGGER.trace("tables({})", tenantId);
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
    LOGGER.trace("delete({}, {})", tenantId, tableName);
    return metrics.time("TenantTableManager.delete", () -> dao.delete(tenantId, tableName));
  }

}
