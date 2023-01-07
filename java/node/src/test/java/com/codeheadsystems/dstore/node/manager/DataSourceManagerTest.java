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

import static com.codeheadsystems.dstore.node.manager.DataSourceManager.INTERNAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.node.engine.DatabaseConnectionEngine;
import com.codeheadsystems.dstore.node.engine.DatabaseInitializationEngine;
import com.codeheadsystems.dstore.node.model.Tenant;
import java.sql.Connection;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TODO: This is more functional as it is testing the liquibase side. Really need a proper liquibase test.
 * ANd yes, it needs to be in the unit test side because we want to run it as quickly as possible.
 * <p>
 * Then, we can make this a proper unit test.
 */
@ExtendWith(MockitoExtension.class)
class DataSourceManagerTest {

  private static final String TENANT_ID = "tenantId";
  private static final String KEY = "key";
  private static final String NONCE = "nonce";
  private static final String CONNECTION_URL = "jdbc:hsqldb:mem:DataSourceManagerTest";

  @Mock private DatabaseInitializationEngine databaseInitializationEngine;
  @Mock private DatabaseConnectionEngine databaseConnectionEngine;
  @Mock private Tenant tenant;
  @Captor private ArgumentCaptor<Connection> connectionArgumentCaptor;

  @InjectMocks private DataSourceManager dataSourceManager;

  @Test
  void loadTenant_realInitialization() {
    when(tenant.id()).thenReturn(TENANT_ID);
    when(tenant.key()).thenReturn(KEY);
    when(tenant.nonce()).thenReturn(NONCE);
    when(databaseConnectionEngine.getTenantConnectionUrl(TENANT_ID, KEY, NONCE)).thenReturn(CONNECTION_URL);
    final DataSource dataSource = dataSourceManager.getDataSource(tenant);
    assertThat(dataSource).isNotNull();
  }

  @Test
  void internalSetup() {
    when(databaseConnectionEngine.getInternalConnectionUrl()).thenReturn(CONNECTION_URL);
    dataSourceManager.start();
    final Optional<DataSource> internalDataSource = dataSourceManager.getInternalDataSource();
    assertThat(internalDataSource)
        .isNotNull()
        .isNotEmpty();
    assertThat(dataSourceManager.isReady()).isTrue();
    verify(databaseInitializationEngine).initialize(connectionArgumentCaptor.capture(), eq(INTERNAL));
  }

}