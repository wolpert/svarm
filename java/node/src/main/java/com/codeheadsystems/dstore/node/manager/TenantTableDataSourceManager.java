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

import com.codeheadsystems.dstore.node.engine.DatabaseEngine;
import com.codeheadsystems.dstore.node.engine.DatabaseInitializationEngine;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides datasources of type tenant. Responsible for generating and maintaining. This caches.
 */
@Singleton
public class TenantTableDataSourceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableDataSourceManager.class);

  private final LoadingCache<TenantTable, DataSource> tenantDataSourceLoadingCache;
  private final DatabaseEngine databaseEngine;
  private final DatabaseInitializationEngine databaseInitializationEngine;

  /**
   * Default constructor for the DSM.
   *
   * @param databaseEngine               to get new data sources.
   * @param databaseInitializationEngine to initialize the database.
   */
  @Inject
  public TenantTableDataSourceManager(final DatabaseEngine databaseEngine,
                                      final DatabaseInitializationEngine databaseInitializationEngine) {
    LOGGER.info("TenantTableDataSourceManager({},{})", databaseEngine, databaseInitializationEngine);
    this.databaseEngine = databaseEngine;
    this.databaseInitializationEngine = databaseInitializationEngine;
    this.tenantDataSourceLoadingCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .removalListener(this::onRemoval)
        .build(CacheLoader.from(this::generate));
  }

  /**
   * Gets the data source for the tenant.
   *
   * @param tenantTable to get the source for.
   * @return the source.
   */
  public DataSource getDataSource(final TenantTable tenantTable) {
    LOGGER.trace("getDataSource({})", tenantTable);
    return tenantDataSourceLoadingCache.getUnchecked(tenantTable);
  }

  /**
   * Returns the current map. Useful for health checks.
   *
   * @return map of the tenants.
   */
  public Map<TenantTable, DataSource> allValues() {
    return tenantDataSourceLoadingCache.asMap();
  }

  /**
   * Removes tenant from the cache.
   *
   * @param tenantTable to remove.
   */
  public void evictTenant(final TenantTable tenantTable) {
    LOGGER.trace("evictTenant({})", tenantTable);
    tenantDataSourceLoadingCache.invalidate(tenantTable);
  }

  private void onRemoval(RemovalNotification<TenantTable, DataSource> notification) {
    LOGGER.debug("onRemoval({},{})", notification.getKey(), notification.getCause());
  }

  /**
   * Generate a new data source for the tenant table. This is not cached.
   *
   * @param tenantTable the tenant table to use.
   * @return the data source.
   */
  private DataSource generate(final TenantTable tenantTable) {
    LOGGER.debug("dataSource({})", tenantTable);
    final DataSource dataSource = databaseEngine.tenantDataSource(tenantTable);
    try {
      LOGGER.trace("Getting connection");
      final Connection connection = dataSource.getConnection();
      databaseInitializationEngine.initialize(connection, tenantTable.tableVersion());
      return dataSource;
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to get tenant initialized connection", e);
    }
  }

  /**
   * Invalidates the cache entry, deletes the database.
   *
   * @param tenantTable to delete.
   */
  public void deleteEverything(final TenantTable tenantTable) {
    LOGGER.info("deleteEverything({})", tenantTable.identifier());
    evictTenant(tenantTable);
    databaseEngine.tenantDataStoreLocation(tenantTable).ifPresentOrElse(databasePath -> {
      LOGGER.warn("Deleting path {}", databasePath);
      //TODO Do the delete, add metrics here.
    }, () -> LOGGER.info("No files to delete for {}", tenantTable.identifier()));
  }
}
