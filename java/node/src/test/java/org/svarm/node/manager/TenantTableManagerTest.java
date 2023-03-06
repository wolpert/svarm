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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.crypt.AesGcmSivManager;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.dao.TenantTableDao;
import org.svarm.node.engine.TableDefinitionEngine;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;
import org.svarm.server.exception.ExceptionUtils;

@ExtendWith(MockitoExtension.class)
class TenantTableManagerTest extends BaseMetricTest {
  private static final String NONCE = "NONCE";
  private static final String KEY = "KEY";
  private static final String TENANT_ID = "tenant";
  private static final String TABLE_NAME = "tablename";
  private static final TableDefinition ENGINE = TableDefinition.V1SingleEntryEngine;
  private static final TenantTableIdentifier IDENTIFIER = TenantTableIdentifier.from(TENANT_ID, TABLE_NAME);

  @Mock private TenantTableDao dao;
  @Mock private AesGcmSivManager aesManager;
  @Mock private TableDefinitionEngine engine;
  @Mock private TenantTable tenantTable;
  @Mock private List<String> list;
  @Mock private TenantTableJdbiManager tenantTableJdbiManager;
  @Captor private ArgumentCaptor<TenantTable> tenantTableArgumentCaptor;
  @Captor private ArgumentCaptor<String> stringArgumentCaptor;

  private TenantTableManager manager;
  private Map<TableDefinition, TableDefinitionEngine> tableDefinitionEngineMap;

  @BeforeEach
  void setup() {
    tableDefinitionEngineMap = Map.of(ENGINE, engine);
    manager = new TenantTableManager(metrics, dao, aesManager, tableDefinitionEngineMap, tenantTableJdbiManager, new ExceptionUtils());
  }

  @Test
  void get() {
    when(dao.read(TENANT_ID, TABLE_NAME)).thenReturn(Optional.of(tenantTable));
    Assertions.assertThat(manager.get(IDENTIFIER)).isPresent().contains(tenantTable);
  }

  @Test
  void create() {
    when(aesManager.randomKeyBase64Encoded()).thenReturn(KEY);
    when(aesManager.randomNonceBase64Encoded()).thenReturn(NONCE);
    Assertions.assertThat(manager.create(IDENTIFIER, ENGINE))
        .hasFieldOrPropertyWithValue("identifier", IDENTIFIER);
    verify(dao).create(tenantTableArgumentCaptor.capture());
    assertThat(tenantTableArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("tableVersion", ENGINE.name())
        .hasFieldOrPropertyWithValue("key", KEY)
        .hasFieldOrPropertyWithValue("nonce", NONCE)
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
    when(dao.read(TENANT_ID, TABLE_NAME)).thenReturn(Optional.of(tenantTable));
    manager.delete(IDENTIFIER);
    verify(dao).delete(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
    assertThat(stringArgumentCaptor.getAllValues())
        .containsExactly(TENANT_ID, TABLE_NAME);
  }
}