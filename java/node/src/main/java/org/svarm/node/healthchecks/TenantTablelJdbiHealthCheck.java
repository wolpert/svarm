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

package org.svarm.node.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.metrics.Metrics;
import java.sql.SQLException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.manager.TenantTableJdbiManager;
import org.svarm.node.model.TenantTable;

/**
 * Verifies the datastore is healthy.
 */
@Singleton
public class TenantTablelJdbiHealthCheck extends HealthCheck {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTablelJdbiHealthCheck.class);

  private final TenantTableJdbiManager tenantTableJdbiManager;
  private final Metrics metrics;

  /**
   * Default health check constructor.
   *
   * @param tenantTableJdbiManager to use.
   * @param metrics                to use.
   */
  @Inject
  public TenantTablelJdbiHealthCheck(final TenantTableJdbiManager tenantTableJdbiManager,
                                     final Metrics metrics) {
    LOGGER.info("InternalDataSourceHealthCheck({})", tenantTableJdbiManager);
    this.tenantTableJdbiManager = tenantTableJdbiManager;
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
      final boolean isHealthy = tenantTableJdbiManager.allValues().entrySet()
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

  private boolean check(final Map.Entry<TenantTable, Jdbi> tenantTableDataSourceEntry) {
    final TenantTable tenantTable = tenantTableDataSourceEntry.getKey();
    try {
      if (tenantTableDataSourceEntry.getValue().withHandle(
          handle -> handle.getConnection().isValid(1))) {
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
