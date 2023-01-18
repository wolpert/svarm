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

import com.codeheadsystems.dstore.node.factory.TenantTableDataSourceFactory;
import com.codeheadsystems.dstore.node.model.Tenant;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides datasources either of type tenant or internal. Responsible for generating and maintaining.
 */
@Singleton
public class DataSourceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceManager.class);

  private final LoadingCache<TenantTable, DataSource> tenantDataSourceLoadingCache;

  /**
   * Default constructor for the DSM.
   *
   * @param tenantTableDataSourceFactory used to create data sources for tenant tables.
   */
  @Inject
  public DataSourceManager(final TenantTableDataSourceFactory tenantTableDataSourceFactory) {
    LOGGER.info("DataSourceManager({})", tenantTableDataSourceFactory);
    this.tenantDataSourceLoadingCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .removalListener(this::onRemoval)
        .build(CacheLoader.from(tenantTableDataSourceFactory::generate));
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
   * @param tenant to remove.
   */
  public void evictTenant(Tenant tenant) {
    LOGGER.trace("evictTenant({})", tenant);
    tenantDataSourceLoadingCache.invalidate(tenant);
  }

  private void onRemoval(RemovalNotification<TenantTable, DataSource> notification) {
    LOGGER.debug("onRemoval({},{})", notification.getKey(), notification.getCause());
  }

}
