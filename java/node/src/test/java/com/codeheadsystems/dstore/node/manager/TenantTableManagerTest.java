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

import com.codeheadsystems.dstore.common.crypt.AesGcmSivManager;
import com.codeheadsystems.dstore.node.dao.TenantTableDao;
import com.codeheadsystems.dstore.node.engine.TableDefinitionEngine;
import com.codeheadsystems.dstore.node.exception.ExceptionUtils;
import com.codeheadsystems.dstore.node.model.ImmutableTenantTableIdentifier;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantTableManagerTest extends BaseMetricTest {
  private static final String NONCE = "NONCE";
  private static final String KEY = "KEY";
  private static final String TENANT_ID = "tenant";
  private static final String TABLE_NAME = "tablename";
  private static final String ENGINE = "engine";
  private static final String PRIMARY_KEY = "primary_key";
  private static final TenantTableIdentifier IDENTIFIER = TenantTableIdentifier.from(TENANT_ID, TABLE_NAME);

  @Mock private TenantTableDao dao;
  @Mock private AesGcmSivManager aesManager;
  @Mock private TableDefinitionEngine engine;
  @Mock private TenantTable tenantTable;
  @Mock private List<String> list;
  @Mock private DataSourceManager dataSourceManager;
  @Captor private ArgumentCaptor<TenantTable> tenantTableArgumentCaptor;
  @Captor private ArgumentCaptor<String> stringArgumentCaptor;

  private TenantTableManager manager;
  private Map<String, TableDefinitionEngine> tableDefinitionEngineMap;

  @BeforeEach
  void setup() {
    tableDefinitionEngineMap = Map.of(ENGINE, engine);
    manager = new TenantTableManager(metrics, dao, aesManager, tableDefinitionEngineMap, dataSourceManager, new ExceptionUtils());
  }

  @Test
  void get() {
    when(dao.read(TENANT_ID, TABLE_NAME)).thenReturn(Optional.of(tenantTable));
    assertThat(manager.get(IDENTIFIER)).isPresent().contains(tenantTable);
  }

  @Test
  void create() {
    when(aesManager.randomKeyBase64Encoded()).thenReturn(KEY);
    when(aesManager.randomNonceBase64Encoded()).thenReturn(NONCE);
    when(dao.create(tenantTableArgumentCaptor.capture())).thenReturn(tenantTable);
    assertThat(manager.create(IDENTIFIER, ENGINE, PRIMARY_KEY)).isEqualTo(tenantTable);
    assertThat(tenantTableArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("tableVersion", ENGINE)
        .hasFieldOrPropertyWithValue("key", KEY)
        .hasFieldOrPropertyWithValue("nonce", NONCE)
        .hasFieldOrPropertyWithValue("primaryKey", PRIMARY_KEY)
        .extracting("identifier")
        .hasFieldOrPropertyWithValue("tenantId", TENANT_ID)
        .hasFieldOrPropertyWithValue("tableName", TABLE_NAME);
  }

  @Test
  void tenants() {
    when(dao.allTenantTables(TENANT_ID)).thenReturn(list);
    assertThat(manager.tables(TENANT_ID)).isEqualTo(list);
  }

  @Test
  void delete() {
    when(dao.delete(stringArgumentCaptor.capture(), stringArgumentCaptor.capture())).thenReturn(true);
    manager.delete(IDENTIFIER);
    assertThat(stringArgumentCaptor.getAllValues())
        .containsExactly(TENANT_ID, TABLE_NAME);
  }
}