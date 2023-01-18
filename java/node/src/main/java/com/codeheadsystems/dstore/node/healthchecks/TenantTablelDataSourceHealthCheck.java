/*
 * Copyright (c) 2022-2023. Ned Wolpert
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

package com.codeheadsystems.dstore.node.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.dstore.node.manager.DataSourceManager;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.metrics.Metrics;
import java.sql.SQLException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies the datastore is healthy.
 */
@Singleton
public class TenantTablelDataSourceHealthCheck extends HealthCheck {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTablelDataSourceHealthCheck.class);

  private final DataSourceManager dataSourceManager;
  private final Metrics metrics;

  /**
   * Default health check constructor.
   *
   * @param dataSourceManager to use.
   * @param metrics           to use.
   */
  @Inject
  public TenantTablelDataSourceHealthCheck(final DataSourceManager dataSourceManager,
                                           final Metrics metrics) {
    LOGGER.info("InternalDataSourceHealthCheck({})", dataSourceManager);
    this.dataSourceManager = dataSourceManager;
    this.metrics = metrics;
  }

  /**
   * Checks the health of the databases.
   *
   * @return the result.
   * @throws Exception if something went wrong.
   */
  @Override
  protected Result check() throws Exception {
    LOGGER.trace("check()");
    try {
      final boolean isHealthy = dataSourceManager.allValues().entrySet()
          .stream()
          .allMatch(this::check);
      if (isHealthy) {
        return Result.healthy();
      } else {
        return Result.unhealthy("soft fail");
      }
    } catch (IllegalStateException e) { // we dead
      return Result.unhealthy("DB Fatal");
    }
  }

  private boolean check(final Map.Entry<TenantTable, DataSource> tenantTableDataSourceEntry) {
    final TenantTable tenantTable = tenantTableDataSourceEntry.getKey();
    final DataSource dataSource = tenantTableDataSourceEntry.getValue();
    try {
      if (dataSource.getConnection().isValid(1)) {
        return true;
      } else {
        LOGGER.warn("Invalid Tenant Table {}", tenantTable);
        return false;
      }
    } catch (SQLException e) {
      LOGGER.error("Database is dead: {}", tenantTable, e);
      throw new IllegalStateException(e);
    }
  }

}
