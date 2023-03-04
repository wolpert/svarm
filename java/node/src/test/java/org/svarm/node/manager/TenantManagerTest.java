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
import org.svarm.node.dao.TenantDao;
import org.svarm.node.model.Tenant;
import org.svarm.server.exception.ExceptionUtils;

@ExtendWith(MockitoExtension.class)
class TenantManagerTest extends BaseMetricTest {
  private static final String NONCE = "NONCE";
  private static final String KEY = "KEY";
  private final String TENANT_ID = "tenant";

  @Mock private TenantDao dao;
  @Mock private AesGcmSivManager aesManager;
  @Mock private Tenant tenant;
  @Mock private List<String> list;
  @Captor private ArgumentCaptor<Tenant> tenantArgumentCaptor;
  @Captor private ArgumentCaptor<String> stringArgumentCaptor;

  private TenantManager manager;

  @BeforeEach
  void setup() {
    manager = new TenantManager(metrics, dao, aesManager, new ExceptionUtils());
  }

  @Test
  void get() {
    when(dao.read(TENANT_ID)).thenReturn(Optional.of(tenant));
    Assertions.assertThat(manager.get(TENANT_ID)).isPresent().contains(tenant);
  }

  @Test
  void create() {
    when(aesManager.randomKeyBase64Encoded()).thenReturn(KEY);
    when(aesManager.randomNonceBase64Encoded()).thenReturn(NONCE);
    Assertions.assertThat(manager.create(TENANT_ID))
        .isNotNull()
        .hasFieldOrPropertyWithValue("ridTenant", TENANT_ID);
    verify(dao).create(tenantArgumentCaptor.capture());
    assertThat(tenantArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("ridTenant", TENANT_ID)
        .hasFieldOrPropertyWithValue("key", KEY)
        .hasFieldOrPropertyWithValue("nonce", NONCE);
  }

  @Test
  void tenants() {
    when(dao.allTenants()).thenReturn(list);
    assertThat(manager.tenants()).isEqualTo(list);
  }

  @Test
  void delete() {
    manager.delete(TENANT_ID);
    verify(dao).delete(stringArgumentCaptor.capture());
    assertThat(stringArgumentCaptor.getValue()).isEqualTo(TENANT_ID);
  }
}