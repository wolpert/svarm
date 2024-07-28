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
import com.google.common.cache.RemovalNotification;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.immutables.value.Value;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.engine.DatabaseEngine;
import org.svarm.node.engine.DatabaseInitializationEngine;
import org.svarm.node.engine.impl.v1singleentry.V1Row;
import org.svarm.node.engine.impl.v1singleentry.V1RowDao;
import org.svarm.node.factory.JdbiFactory;
import org.svarm.node.model.TenantTable;
import org.svarm.node.utils.TagHelper;

/**
 * Provides datasources of type tenant. Responsible for generating and maintaining. This caches.
 */
@Singleton
public class TenantTableJdbiManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableJdbiManager.class);

  private final LoadingCache<TenantTable, CacheHolder> jdbiLoadingCache;
  private final DatabaseEngine databaseEngine;
  private final DatabaseInitializationEngine databaseInitializationEngine;
  private final Metrics metrics;
  private final JdbiFactory jdbiFactory;

  /**
   * Default constructor for the DSM.
   *
   * @param databaseEngine               to get new data sources.
   * @param databaseInitializationEngine to initialize the database.
   * @param metrics                      to track.
   * @param jdbiFactory                  to create jdbi connections.
   */
  @Inject
  public TenantTableJdbiManager(final DatabaseEngine databaseEngine,
                                final DatabaseInitializationEngine databaseInitializationEngine,
                                final Metrics metrics,
                                final JdbiFactory jdbiFactory) {
    LOGGER.info("TenantTableJdbiManager({},{},{},{})",
        databaseEngine, databaseInitializationEngine, metrics, jdbiFactory);
    this.metrics = metrics;
    this.databaseEngine = databaseEngine;
    this.databaseInitializationEngine = databaseInitializationEngine;
    this.jdbiFactory = jdbiFactory;
    this.jdbiLoadingCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .removalListener(this::onRemoval)
        .build(CacheLoader.from(this::generateJdbi));
  }


  /**
   * Returns the current map. Useful for health checks.
   *
   * @return map of the tenants.
   */
  public Map<TenantTable, Jdbi> allValues() {
    return jdbiLoadingCache.asMap().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().jdbi()));
  }

  /**
   * A method that retrieves the data source/jdbi instance. If it does not exist,
   * it blows up.
   *
   * @param tenantTable to verify.
   */
  public void ensureDataStoreCreated(final TenantTable tenantTable) {
    LOGGER.trace("ensureDataStoreCreated({})", tenantTable);
    if (getJdbi(tenantTable) == null) {
      throw new IllegalStateException("No such data source");
    }
  }

  /**
   * Gets the data source for the tenant.
   *
   * @param tenantTable to get the source for.
   * @return the source.
   */
  public Jdbi getJdbi(final TenantTable tenantTable) {
    LOGGER.trace("getDataSource({})", tenantTable);
    metrics.increment("TenantTableJdbiManager.getJdbi", TagHelper.from(tenantTable));
    return jdbiLoadingCache.getUnchecked(tenantTable).jdbi();
  }

  /**
   * Gets the v1 row dao for the tenant.
   *
   * @param tenantTable to get the source for.
   * @return the source.
   */
  public V1RowDao getV1RowDao(final TenantTable tenantTable) {
    LOGGER.trace("getV1RowDao({})", tenantTable);
    metrics.increment("TenantTableJdbiManager.getV1RowDao", TagHelper.from(tenantTable));
    return jdbiLoadingCache.getUnchecked(tenantTable).v1RowDao();
  }

  /**
   * Removes tenant from the cache.
   *
   * @param tenantTable to remove.
   */
  public void evictTenant(final TenantTable tenantTable) {
    LOGGER.trace("evictTenant({})", tenantTable);
    metrics.increment("TenantTableJdbiManager.evictTenant", TagHelper.from(tenantTable));
    jdbiLoadingCache.invalidate(tenantTable);
  }

  private void onRemoval(RemovalNotification<TenantTable, CacheHolder> notification) {
    LOGGER.debug("onRemoval({},{})", notification.getKey(), notification.getCause());
    metrics.increment("TenantTableJdbiManager.onRemoval", TagHelper.from(notification.getKey()));
    notification.getValue().jdbi().withHandle(handle -> handle.execute("shutdown;"));
  }


  private CacheHolder generateJdbi(final TenantTable tenantTable) {
    final DataSource dataSource = generateDataSource(tenantTable);
    final Jdbi jdbi = jdbiFactory.generate(dataSource);
    jdbi.installPlugin(new SqlObjectPlugin());
    jdbi.getConfig(JdbiImmutables.class)
        .registerImmutable(V1Row.class);
    return ImmutableCacheHolder.builder()
        .jdbi(jdbi)
        .v1RowDao(jdbi.onDemand(V1RowDao.class))
        .build();
  }

  /**
   * Generate a new data source for the tenant table. This is not cached.
   *
   * @param tenantTable the tenant table to use.
   * @return the data source.
   */
  private DataSource generateDataSource(final TenantTable tenantTable) {
    LOGGER.debug("generateDataSource({})", tenantTable);
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
    metrics.increment("TenantTableJdbiManager.deleteEverything", TagHelper.from(tenantTable));
    evictTenant(tenantTable);
    databaseEngine.deleteTenantDataStoreLocation(tenantTable);
  }

  /**
   * The interface Cache holder.
   */
  @Value.Immutable
  interface CacheHolder {

    /**
     * Jdbi jdbi.
     *
     * @return the jdbi
     */
    Jdbi jdbi();

    V1RowDao v1RowDao();

  }
}
