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

package org.svarm.node.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.node.BaseSQLTest;
import org.svarm.node.model.ImmutableTenant;
import org.svarm.node.model.Tenant;

class TenantDaoTest extends BaseSQLTest {

  private TenantDao dao;

  @BeforeEach
  void setup() {
    dao = internalJdbi.onDemand(TenantDao.class);
  }

  @Test
  void roundTrip() {
    final Tenant tenant = randomTenant();
    Assertions.assertThat(dao.read(tenant.ridTenant())).isEmpty();
    dao.create(tenant);
    Assertions.assertThat(dao.read(tenant.ridTenant())).isPresent().contains(tenant);
  }

  @Test
  void list() {
    final List<Tenant> tenants = List.of(randomTenant(), randomTenant(), randomTenant());
    tenants.forEach(dao::create);
    final List<String> ids = tenants.stream().map(Tenant::ridTenant).toList();
    assertThat(dao.allTenants()).containsOnlyOnceElementsOf(ids);
  }

  @Test
  void delete() {
    final Tenant tenant = randomTenant();
    Assertions.assertThat(dao.read(tenant.ridTenant())).isEmpty();
    dao.create(tenant);
    Assertions.assertThat(dao.read(tenant.ridTenant())).isPresent().contains(tenant);
    dao.delete(tenant.ridTenant());
    Assertions.assertThat(dao.read(tenant.ridTenant())).isEmpty();
  }

  private Tenant randomTenant() {
    return ImmutableTenant.builder()
        .ridTenant(UUID.randomUUID().toString())
        .nonce(UUID.randomUUID().toString())
        .key(UUID.randomUUID().toString())
        .uuid(UUID.randomUUID().toString())
        .build();
  }
}