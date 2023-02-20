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

package org.svarm.node.module;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jdbi3.InstrumentedSqlLogger;
import dagger.Module;
import dagger.Provides;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;
import org.svarm.node.engine.DatabaseEngine;
import org.svarm.node.engine.DatabaseInitializationEngine;
import org.svarm.node.model.Tenant;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * Provides data sources. We pick the database to use here.
 */
@Module(includes = {HsqlDataSourceModule.class})
public class DataSourceModule {

  /**
   * Identifier for internal liquibase files.
   */
  public static final String INTERNAL = "internal";

  /**
   * Returns a built, usable data source.
   *
   * @param databaseEngine               database  engine.
   * @param databaseInitializationEngine database initialization engine.
   * @return the datasource.
   */
  @Provides
  @Singleton
  public DataSource internalDataSource(final DatabaseEngine databaseEngine,
                                       final DatabaseInitializationEngine databaseInitializationEngine) {
    final DataSource dataSource = databaseEngine.internalDataSource();
    final Connection connection;
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
    databaseInitializationEngine.initialize(connection, INTERNAL);
    return dataSource;
  }

  /**
   * The internal jdbi instance.
   *
   * @param dataSource     to use.
   * @param metricRegistry for metrics.
   * @return a jdbi instance.
   */
  @Provides
  @Singleton
  public Jdbi internalJdbi(final DataSource dataSource,
                           final MetricRegistry metricRegistry) {
    final Jdbi jdbi = Jdbi.create(dataSource);
    jdbi.getConfig(JdbiImmutables.class)
        .registerImmutable(Tenant.class)
        .registerImmutable(TenantTable.class)
        .registerImmutable(TenantTableIdentifier.class);
    jdbi.setSqlLogger(new InstrumentedSqlLogger(metricRegistry));
    return jdbi;
  }
}