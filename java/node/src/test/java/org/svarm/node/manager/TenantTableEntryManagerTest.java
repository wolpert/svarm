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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.engine.TableDefinitionEngine;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;
import org.svarm.server.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class TenantTableEntryManagerTest {

  private static final String TABLE_VERSION = "ENGINE";
  private static final String ENTITY = "entity";
  @Mock private TableDefinitionEngine tableDefinitionEngine;
  @Mock private TenantTableManager tenantTableManager;
  @Mock private JsonNode jsonNode;
  @Mock private TenantTableIdentifier identifier;
  @Mock private TenantTable tenantTable;
  @Mock private EntryInfo entryInfo;

  @Captor private ArgumentCaptor<TenantTable> tableArgumentCaptor;
  @Captor private ArgumentCaptor<EntryInfo> entryInfoArgumentCaptor;

  private TenantTableEntryManager manager;

  @BeforeEach
  void setup() {
    final Map<String, TableDefinitionEngine> map = ImmutableMap.of(TABLE_VERSION, tableDefinitionEngine);
    manager = new TenantTableEntryManager(map, tenantTableManager);
  }

  @Test
  void read_tableFound() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.of(tenantTable));
    when(tenantTable.tableVersion()).thenReturn(TABLE_VERSION);
    when(tableDefinitionEngine.read(tenantTable, ENTITY)).thenReturn(Optional.of(entryInfo));

    assertThat(manager.read(identifier, ENTITY))
        .isPresent()
        .contains(entryInfo);
  }

  @Test
  void read_tableFound_noEngine() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.of(tenantTable));
    when(tenantTable.tableVersion()).thenReturn(TABLE_VERSION + " not found");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.read(identifier, ENTITY));
  }

  @Test
  void read_tableNotFound() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.empty());
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> manager.read(identifier, ENTITY));
  }

  @Test
  void write_tableFound() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.of(tenantTable));
    when(tenantTable.tableVersion()).thenReturn(TABLE_VERSION);

    manager.write(identifier, entryInfo);
    verify(tableDefinitionEngine).write(tableArgumentCaptor.capture(), entryInfoArgumentCaptor.capture());
    assertThat(tableArgumentCaptor.getValue()).isEqualTo(tenantTable);
    assertThat(entryInfoArgumentCaptor.getValue()).isEqualTo(entryInfo);
  }

  @Test
  void write_tableFound_noEngine() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.of(tenantTable));
    when(tenantTable.tableVersion()).thenReturn(TABLE_VERSION + " not found");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.write(identifier, entryInfo));
  }

  @Test
  void write_tableNotFound() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.empty());
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> manager.write(identifier, entryInfo));
  }

  @Test
  void delete_tableFound() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.of(tenantTable));
    when(tenantTable.tableVersion()).thenReturn(TABLE_VERSION);
    when(tableDefinitionEngine.delete(tenantTable, ENTITY)).thenReturn(true);

    assertThat(manager.delete(identifier, ENTITY)).isTrue();
  }

  @Test
  void delete_tableFound_noEngine() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.of(tenantTable));
    when(tenantTable.tableVersion()).thenReturn(TABLE_VERSION + " not found");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.delete(identifier, ENTITY));
  }

  @Test
  void delete_tableNotFound() {
    when(tenantTableManager.get(identifier)).thenReturn(Optional.empty());
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> manager.delete(identifier, ENTITY));
  }
}