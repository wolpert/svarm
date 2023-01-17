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
import com.codeheadsystems.metrics.Metrics;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies the internal datastore is healthy.
 */
@Singleton
public class InternalDataSourceHealthCheck extends HealthCheck {

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalDataSourceHealthCheck.class);

  private final DataSource internalDataSource;
  private final Metrics metrics;

  /**
   * Default health check.
   *
   * @param internalDataSource to use.
   * @param metrics            to use.
   */
  @Inject
  public InternalDataSourceHealthCheck(final DataSource internalDataSource,
                                       final Metrics metrics) {
    LOGGER.info("InternalDataSourceHealthCheck({})", internalDataSource);
    this.internalDataSource = internalDataSource;
    this.metrics = metrics;
  }

  /**
   * Checks the health of the internal database.
   *
   * @return the result.
   * @throws Exception if something went wrong.
   */
  @Override
  protected Result check() throws Exception {
    LOGGER.trace("check()");
    try {
      if (internalDataSource.getConnection().isValid(1)) {
        return Result.healthy();
      } else {
        return Result.unhealthy("DB Unavailable");
      }
    } catch (SQLException e) {
      LOGGER.error("Database check failure", e);
      return Result.unhealthy("DB Fatal");
    }
  }
}
