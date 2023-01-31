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

package com.codeheadsystems.dstore.node.resource;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.node.api.TenantTableInfo;
import com.codeheadsystems.dstore.node.converter.TenantTableInfoConverter;
import com.codeheadsystems.dstore.node.engine.impl.V1SingleEntryEngine;
import com.codeheadsystems.dstore.node.manager.TenantTableManager;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

  @InjectMocks private TenantTableResource resource;

  @Test
  void list() {
    when(tenantTableManager.tables(TENANT)).thenReturn(list);
    assertThat(resource.list(TENANT)).isEqualTo(list);
  }

  @Test
  void read_found() {
    when(tenantTableManager.get(IDENTIFIER)).thenReturn(Optional.of(tenantTable));
    when(tenantTable.identifier()).thenReturn(identifier);
    when(tenantTableInfoConverter.from(identifier)).thenReturn(tenantTableInfo);
    assertThat(resource.read(TENANT, TABLE_NAME))
        .isNotNull()
        .isPresent()
        .contains(tenantTableInfo);
  }

  @Test
  void read_notFound() {
    assertThat(resource.read(TENANT, TABLE_NAME))
        .isNotNull()
        .isNotPresent();
  }

  @Test
  void create() {
    when(tenantTableManager.create(IDENTIFIER, V1SingleEntryEngine.DEFINITION_NAME))
        .thenReturn(tenantTable);
    when(tenantTable.identifier()).thenReturn(identifier);
    when(tenantTableInfoConverter.from(identifier)).thenReturn(tenantTableInfo);
    assertThat(resource.create(TENANT, TABLE_NAME))
        .isEqualTo(tenantTableInfo);
  }

  @Test
  void delete_found() {
    when(tenantTableManager.delete(IDENTIFIER)).thenReturn(true);
    assertThat(resource.delete(TENANT, TABLE_NAME))
        .hasFieldOrPropertyWithValue("status", Response.Status.NO_CONTENT.getStatusCode());
  }

  @Test
  void delete_notFound() {
    assertThat(resource.delete(TENANT, TABLE_NAME))
        .hasFieldOrPropertyWithValue("status", Response.Status.NOT_FOUND.getStatusCode());
  }

}