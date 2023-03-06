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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.node.engine.DatabaseEngine;
import org.svarm.node.engine.DatabaseInitializationEngine;
import org.svarm.node.factory.JdbiFactory;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * TODO: This is more functional as it is testing the liquibase side. Really need a proper liquibase test.
 * ANd yes, it needs to be in the unit test side because we want to run it as quickly as possible.
 * <p>
 * Then, we can make this a proper unit test.
 */
@ExtendWith(MockitoExtension.class)
class TenantTableJdbiManagerTest extends BaseMetricTest {

  private static final String TENANT_ID = "tenantId";
  private static final String TABLE_NAME = "tableName";
  @Mock private TenantTable tenantTable;
  @Mock private TenantTableIdentifier identifier;
  @Mock private DataSource dataSource;
  @Mock private DatabaseInitializationEngine databaseInitializationEngine;
  @Mock private DatabaseEngine databaseEngine;
  @Mock private JdbiFactory jdbiFactory;
  @Mock private Jdbi jdbi;

  private TenantTableJdbiManager tenantTableJdbiManager;

  @BeforeEach
  public void setup() {
    tenantTableJdbiManager = new TenantTableJdbiManager(databaseEngine, databaseInitializationEngine, metrics, jdbiFactory);
  }

  @Test
  void loadTenant_realInitialization() {
    when(databaseEngine.tenantDataSource(tenantTable)).thenReturn(dataSource);
    when(jdbiFactory.generate(dataSource)).thenReturn(jdbi);
    when(tenantTable.identifier()).thenReturn(identifier);
    when(identifier.tenantId()).thenReturn(TENANT_ID);
    when(identifier.tableName()).thenReturn(TABLE_NAME);
    final Jdbi result = tenantTableJdbiManager.getJdbi(tenantTable);
    assertThat(result).isEqualTo(jdbi);
  }

}