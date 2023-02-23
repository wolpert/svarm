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

package org.svarm.node.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.node.api.TableMetaData;
import org.svarm.node.api.TenantTableInfo;
import org.svarm.node.converter.TenantTableInfoConverter;
import org.svarm.node.engine.impl.V1SingleEntryEngine;
import org.svarm.node.manager.TenantTableManager;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;
import org.svarm.server.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class TenantTableResourceTest {

  private static final String TENANT = "TENANT";
  private static final String TABLE_NAME = "table name";
  private static final TenantTableIdentifier IDENTIFIER = TenantTableIdentifier.from(TENANT, TABLE_NAME);
  @Mock private TenantTableManager tenantTableManager;
  @Mock private TenantTableInfoConverter tenantTableInfoConverter;
  @Mock private TenantTableInfo tenantTableInfo;
  @Mock private TenantTableIdentifier identifier;
  @Mock private TenantTable tenantTable;
  @Mock private List<String> list;
  @Mock private TableMetaData tableMetaData;

  @InjectMocks private TenantTableResource resource;

  @Test
  void list() {
    when(tenantTableManager.tables(TENANT)).thenReturn(list);
    assertThat(resource.listTenantTables(TENANT)).isEqualTo(list);
  }

  @Test
  void read_found() {
    when(tenantTableManager.get(IDENTIFIER)).thenReturn(Optional.of(tenantTable));
    when(tenantTable.identifier()).thenReturn(identifier);
    when(tenantTableInfoConverter.from(identifier)).thenReturn(tenantTableInfo);
    assertThat(resource.readTenantTable(TENANT, TABLE_NAME))
        .isNotNull()
        .isPresent()
        .contains(tenantTableInfo);
  }

  @Test
  void read_notFound() {
    assertThat(resource.readTenantTable(TENANT, TABLE_NAME))
        .isNotNull()
        .isNotPresent();
  }

  @Test
  void create() {
    when(tenantTableManager.create(IDENTIFIER, V1SingleEntryEngine.DEFINITION_NAME))
        .thenReturn(tenantTable);
    when(tenantTable.identifier()).thenReturn(identifier);
    when(tenantTableInfoConverter.from(identifier)).thenReturn(tenantTableInfo);
    assertThat(resource.createTenantTable(TENANT, TABLE_NAME, tableMetaData))
        .isEqualTo(tenantTableInfo);
  }

  @Test
  void delete_found() {
    when(tenantTableManager.delete(IDENTIFIER)).thenReturn(true);
    assertThatNoException().isThrownBy(() -> resource.deleteTenantTable(TENANT, TABLE_NAME));
  }

  @Test
  void delete_notFound() {
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> resource.deleteTenantTable(TENANT, TABLE_NAME));
  }

}