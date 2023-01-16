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

package com.codeheadsystems.dstore.node;

import static org.mockito.Mockito.lenient;

import com.codeheadsystems.dstore.node.engine.DatabaseConnectionEngine;
import com.codeheadsystems.dstore.node.engine.DatabaseInitializationEngine;
import com.codeheadsystems.dstore.node.engine.SqlEngine;
import com.codeheadsystems.dstore.node.manager.DataSourceManager;
import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public abstract class BaseSQLTest extends BaseMetricTest {

  private static final Logger log = LoggerFactory.getLogger(BaseSQLTest.class);

  protected SqlEngine sqlEngine;
  protected DataSourceManager dataSourceManager;
  private DatabaseInitializationEngine databaseInitializationEngine;
  @Mock private DatabaseConnectionEngine databaseConnectionEngine;

  @BeforeEach
  void setupSQLEngine() throws SQLException {
    databaseInitializationEngine = new DatabaseInitializationEngine();
    final String url = "jdbc:hsqldb:mem:" + getClass().getSimpleName() + ":" + UUID.randomUUID();
    log.info("Init {}", url);
    lenient().when(databaseConnectionEngine.getInternalConnectionUrl()).thenReturn(url);
    dataSourceManager = new DataSourceManager(databaseConnectionEngine, databaseInitializationEngine);
    dataSourceManager.start();
    sqlEngine = new SqlEngine(metrics, dataSourceManager);
  }

  @AfterEach
  void shutdownSQLEngine() throws SQLException {
    dataSourceManager.getInternalDataSource().get().getConnection().createStatement().execute("shutdown;");
  }

}
