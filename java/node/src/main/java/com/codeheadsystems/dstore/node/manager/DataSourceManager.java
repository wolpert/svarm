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

import com.codeheadsystems.dstore.node.engine.DatabaseConnectionEngine;
import com.codeheadsystems.dstore.node.engine.DatabaseInitializationEngine;
import com.codeheadsystems.dstore.node.model.Tenant;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import io.dropwizard.lifecycle.Managed;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides datasources either of type tenant or internal. Responsible for generating and maintaining.
 */
@Singleton
public class DataSourceManager {
  /**
   * Identifier for tenant liquibase files.
   */
  public static final String TENANT = "liquibase/V1SingleEntryEngine";
  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceManager.class);
  private static final int INTERNAL_MIN_POOL_SIZE = 1;
  private static final int TENANT_MIN_POOL_SIZE = 0;

  private final DatabaseConnectionEngine databaseConnectionEngine;
  private final DatabaseInitializationEngine databaseInitializationEngine;
  private final LoadingCache<TenantTable, DataSource> tenantDataSourceLoadingCache;

  private final DataSource internalDataSource;
  private final Jdbi internalJdbi;

  /**
   * Default constructor for the DSM.
   *
   * @param databaseConnectionEngine     used to create URLs to the database to connect to.
   * @param databaseInitializationEngine Used to initialize the database with default tables.
   * @param internalDataSource           internal data source to use.
   * @param internalJdbi                 internal jdbi to use.
   */
  @Inject
  public DataSourceManager(final DatabaseConnectionEngine databaseConnectionEngine,
                           final DatabaseInitializationEngine databaseInitializationEngine,
                           final DataSource internalDataSource,
                           final Jdbi internalJdbi) {
    LOGGER.info("DataSourceManager({},{})", databaseConnectionEngine, databaseInitializationEngine);
    this.internalJdbi = internalJdbi;
    this.internalDataSource = internalDataSource;
    this.databaseConnectionEngine = databaseConnectionEngine;
    this.databaseInitializationEngine = databaseInitializationEngine;
    this.tenantDataSourceLoadingCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .removalListener(this::onRemoval)
        .build(CacheLoader.from(this::loadTenantTable));
  }

  private static DataSource getComboPooledDataSource(final int minPoolSize, final String url) {
    LOGGER.trace("getComboPooledDataSource() (If you are stuck here, Likely AES failure, your db and keys do not match)");
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(url);
    cpds.setUser("SA");
    cpds.setPassword("");
    cpds.setMinPoolSize(minPoolSize);
    cpds.setAcquireIncrement(5);
    cpds.setMaxPoolSize(20);
    cpds.setMaxIdleTime(300);
    cpds.setTestConnectionOnCheckout(true);
    return cpds;
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
   * Removes tenant from the cache.
   *
   * @param tenant to remove.
   */
  public void evictTenant(Tenant tenant) {
    LOGGER.trace("evictTenant({})", tenant);
    tenantDataSourceLoadingCache.invalidate(tenant);
  }

  private void onRemoval(RemovalNotification<TenantTable, DataSource> notification) {
    LOGGER.debug("onRemoval({},{})", notification.getKey(), notification.getCause());
  }

  private DataSource loadTenantTable(final TenantTable tenantTable) {
    LOGGER.debug("loadTenantTable({})", tenantTable);
    final String url = databaseConnectionEngine.getTenantConnectionUrl(tenantTable);
    final DataSource dataSource = getComboPooledDataSource(TENANT_MIN_POOL_SIZE, url);
    try {
      LOGGER.trace("Getting connection");
      final Connection connection = dataSource.getConnection();
      databaseInitializationEngine.initialize(connection, TENANT);
      return dataSource;
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to get tenant initialized connection", e);
    }
  }

  /**
   * Lets us know if we're healthy.
   *
   * @return boolean if healthy.
   * @throws SQLException if we're fucked.
   */
  public boolean isHealthy() throws SQLException {
    final boolean result = internalDataSource.getConnection().isValid(INTERNAL_MIN_POOL_SIZE);
    if (result) {
      LOGGER.trace("isHealthy(): true");
    } else {
      LOGGER.error("isHealthy(): not valid connection");
    }
    return result;
  }
}
