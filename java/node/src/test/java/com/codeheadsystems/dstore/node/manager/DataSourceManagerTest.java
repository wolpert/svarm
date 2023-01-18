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

import com.codeheadsystems.dstore.node.factory.TenantTableDataSourceFactory;
import com.codeheadsystems.dstore.node.model.TenantTable;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  @Mock private TenantTable tenantTable;

  @Mock private DataSource dataSource;

  @Mock private TenantTableDataSourceFactory factory;

  @InjectMocks private DataSourceManager dataSourceManager;

  @Test
  void loadTenant_realInitialization() {
    when(factory.generate(tenantTable)).thenReturn(dataSource);
    final DataSource dataSource = dataSourceManager.getDataSource(tenantTable);
    assertThat(dataSource).isEqualTo(dataSource);
  }

}