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

package org.svarm.node;

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.metrics.test.BaseMetricTest;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.util.UUID;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.engine.DatabaseEngine;
import org.svarm.node.engine.DatabaseInitializationEngine;
import org.svarm.node.factory.JdbiFactory;
import org.svarm.node.manager.TenantTableJdbiManager;
import org.svarm.node.model.TenantTable;
import org.svarm.node.module.DataSourceModule;

@ExtendWith(MockitoExtension.class)
public abstract class BaseSQLTest extends BaseMetricTest {

  private static final Logger log = LoggerFactory.getLogger(BaseSQLTest.class);

  protected TenantTableJdbiManager tenantTableJdbiManager;
  protected DataSource internalDataSource;
  protected Jdbi internalJdbi;
  protected JdbiFactory jdbiFactory;
  protected DatabaseEngine databaseEngine;

  @BeforeEach
  void setupSQLEngine() {
    final DatabaseInitializationEngine databaseInitializationEngine = new DatabaseInitializationEngine();
    databaseEngine = databaseEngine();
    final MetricRegistry metricRegistry = new MetricRegistry();
    jdbiFactory = new JdbiFactory(metricRegistry);
    final DataSourceModule dataSourceModule = new DataSourceModule();
    internalDataSource = dataSourceModule.internalDataSource(databaseEngine, databaseInitializationEngine);
    internalJdbi = dataSourceModule.internalJdbi(internalDataSource, jdbiFactory);
    tenantTableJdbiManager = new TenantTableJdbiManager(databaseEngine, databaseInitializationEngine, metrics, jdbiFactory);
  }

  private DatabaseEngine databaseEngine() {
    return new DatabaseEngine() {
      @Override
      public DataSource tenantDataSource(final TenantTable table) {
        return dataSource();
      }

      @Override
      public DataSource internalDataSource() {
        return dataSource();
      }
    };
  }

  private DataSource dataSource() {
    final String url = "jdbc:hsqldb:mem:" + getClass().getSimpleName() + ":" + UUID.randomUUID();
    log.info("Init {}", url);
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(url);
    cpds.setUser("SA");
    cpds.setPassword("");
    cpds.setMinPoolSize(0);
    cpds.setAcquireIncrement(10);
    cpds.setMaxPoolSize(40);
    cpds.setMaxIdleTime(300);
    return cpds;
  }

  @AfterEach
  void shutdownSQLEngine() {
    Jdbi.create(internalDataSource).withHandle(handle -> handle.execute("shutdown;"));
  }

}
