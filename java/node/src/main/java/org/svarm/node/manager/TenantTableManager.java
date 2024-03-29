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

package org.svarm.node.manager;

import com.codeheadsystems.metrics.Metrics;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.crypt.AesGcmSivManager;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.dao.TenantTableDao;
import org.svarm.node.engine.TableDefinitionEngine;
import org.svarm.node.model.ImmutableTenantTable;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;
import org.svarm.server.exception.ExceptionUtils;
import org.svarm.server.exception.NotFoundException;

/**
 * Manages tenant tables.
 */
@Singleton
public class TenantTableManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableManager.class);

  private final Metrics metrics;
  private final TenantTableDao dao;
  private final AesGcmSivManager aesGcmSivManager;
  private final Map<TableDefinition, TableDefinitionEngine> tableDefinitionEngineMap;
  private final LoadingCache<TenantTableIdentifier, TenantTable> tenantTableCacheLoader;
  private final TenantTableJdbiManager tenantTableJdbiManager;
  private final ExceptionUtils exceptionUtils;

  /**
   * Default constructor.
   *
   * @param metrics                  to use.
   * @param dao                      to use.
   * @param aesGcmSivManager         to crypt controls.
   * @param tableDefinitionEngineMap map of available engines.
   * @param tenantTableJdbiManager   to ensure the data source exists.
   * @param exceptionUtils           for exception processing.
   */
  @Inject
  public TenantTableManager(final Metrics metrics,
                            final TenantTableDao dao,
                            final AesGcmSivManager aesGcmSivManager,
                            final Map<TableDefinition, TableDefinitionEngine> tableDefinitionEngineMap,
                            final TenantTableJdbiManager tenantTableJdbiManager,
                            final ExceptionUtils exceptionUtils) {
    LOGGER.info("TenantManager({},{},{},{})", metrics, dao, aesGcmSivManager, tableDefinitionEngineMap);
    this.exceptionUtils = exceptionUtils;
    this.tenantTableJdbiManager = tenantTableJdbiManager;
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
   * @param identifier to get.
   * @return the tenant.
   */
  public Optional<TenantTable> get(final TenantTableIdentifier identifier) {
    LOGGER.trace("get({})", identifier);
    return exceptionUtils.loadingCacheExecutionExceptionWrapper(() -> tenantTableCacheLoader.get(identifier));
  }

  private TenantTable load(final TenantTableIdentifier identifier) {
    return metrics.time("TenantTableManager.load", () -> dao.read(identifier.tenantId(), identifier.tableName()))
        .orElseThrow(() -> new NotFoundException("No such tenant table: " + identifier));
  }

  /**
   * Created the tenant table. If it already exists, simply return the one we already have. Idempotent. Does not set the
   * hash values.
   *
   * @param identifier      Table to create.
   * @param tableDefinition the version of the table we are creating.
   * @return a tenant.
   */
  public TenantTable create(final TenantTableIdentifier identifier,
                            final TableDefinition tableDefinition) {
    LOGGER.debug("create({}, {})", identifier, tableDefinition);
    return get(identifier).orElseGet(() ->
        metrics.time("TenantTableManager.create",
            () -> buildTenantTable(identifier, tableDefinition)));
  }

  private TenantTable buildTenantTable(final TenantTableIdentifier identifier,
                                       final TableDefinition tableDefinition) {
    LOGGER.debug("buildTenantTable({}, {})", identifier, tableDefinition);
    if (!tableDefinitionEngineMap.containsKey(tableDefinition)) {
      throw new IllegalArgumentException("Unknown table version: " + tableDefinition);
    }
    final TenantTable tenantTable = ImmutableTenantTable.builder()
        .identifier(identifier)
        .enabled(true)
        .estimatedQuantity(0)
        .tableVersion(tableDefinition.name())
        .key(aesGcmSivManager.randomKeyBase64Encoded())
        .nonce(aesGcmSivManager.randomNonceBase64Encoded())
        .build();
    try {
      dao.create(tenantTable);
      tenantTableJdbiManager.ensureDataStoreCreated(tenantTable);
      return tenantTable;
    } catch (RuntimeException re) {
      LOGGER.error("Unable to create data source for {}, destroying", tenantTable, re);
      dao.delete(identifier.tenantId(), identifier.tableName());
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
   *
   * @param identifier to delete.
   * @return boolean if deleted or not.
   */
  public boolean delete(final TenantTableIdentifier identifier) {
    LOGGER.trace("delete({}})", identifier);
    return metrics.time("TenantTableManager.delete",
        () -> {
          final Optional<TenantTable> tenantTable = get(identifier);
          if (tenantTable.isEmpty()) {
            return false;
          }
          dao.delete(identifier.tenantId(), identifier.tableName());
          tenantTableJdbiManager.deleteEverything(tenantTable.get());
          return true;
        });
  }

}
