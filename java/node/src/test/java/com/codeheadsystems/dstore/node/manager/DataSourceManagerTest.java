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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.node.engine.DatabaseConnectionEngine;
import com.codeheadsystems.dstore.node.engine.DatabaseInitializationEngine;
import com.codeheadsystems.dstore.node.model.Tenant;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataSourceManagerTest {

  private static final String TENANT_ID = "tenantId";
  private static final String KEY = "key";
  private static final String NONCE = "nonce";

  @Mock private DatabaseInitializationEngine databaseInitializationEngine;
  @Mock private DatabaseConnectionEngine databaseConnectionEngine;
  @Mock private Tenant tenant;

  @InjectMocks private DataSourceManager dataSourceManager;

  @Test
  void loadTenant_realInitiaztion() throws SQLException {
    when(tenant.id()).thenReturn(TENANT_ID);
    when(tenant.key()).thenReturn(KEY);
    when(tenant.nonce()).thenReturn(NONCE);
    when(databaseConnectionEngine.getTenantConnectionUrl(TENANT_ID, KEY, NONCE)).thenReturn("jdbc:hsqldb:mem:DataSourceManagerTest-loadTenant");
    final DataSourceManager manager = new DataSourceManager(databaseConnectionEngine, new DatabaseInitializationEngine());
    final DataSource dataSource = manager.getDataSource(tenant);
    assertThat(dataSource)
        .isNotNull();
    final HashSet<String> tableNames = new HashSet<>();
    try (ResultSet result = dataSource.getConnection().createStatement().
        executeQuery("SELECT TABLE_NAME  FROM INFORMATION_SCHEMA.SYSTEM_TABLES where TABLE_SCHEM = 'PUBLIC'")) {
      while (result.next()) {
        tableNames.add(result.getString("TABLE_NAME"));
      }
    }
    assertThat(tableNames)
        .containsExactlyInAnyOrder("DATABASECHANGELOGLOCK", "DATABASECHANGELOG");
  }

  @Test
  void internalSetup() throws SQLException {
    when(databaseConnectionEngine.getInternalConnectionUrl()).thenReturn("jdbc:hsqldb:mem:DataSourceManagerTest");
    final DataSourceManager manager = new DataSourceManager(databaseConnectionEngine, new DatabaseInitializationEngine());
    manager.start();
    final Optional<DataSource> internalDataSource = manager.getInternalDataSource();
    assertThat(internalDataSource)
        .isNotNull()
        .isNotEmpty();
    assertThat(manager.isReady()).isTrue();

    final HashSet<String> tableNames = new HashSet<>();
    try (ResultSet result = internalDataSource.get().getConnection().createStatement().
        executeQuery("SELECT TABLE_NAME  FROM INFORMATION_SCHEMA.SYSTEM_TABLES where TABLE_SCHEM = 'PUBLIC'")) {
      while (result.next()) {
        tableNames.add(result.getString("TABLE_NAME"));
      }
    }
    assertThat(tableNames)
        .containsExactlyInAnyOrder("DATABASECHANGELOGLOCK", "DATABASECHANGELOG", "NODE_TENANT", "NODE_TENANT_TABLES");
  }

}