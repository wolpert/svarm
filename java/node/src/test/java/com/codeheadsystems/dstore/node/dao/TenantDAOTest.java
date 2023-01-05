package com.codeheadsystems.dstore.node.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.dstore.node.BaseSQLTest;
import com.codeheadsystems.dstore.node.model.ImmutableTenant;
import com.codeheadsystems.dstore.node.model.Tenant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantDAOTest extends BaseSQLTest {

  private TenantDAO dao;

  @BeforeEach
  void setup() {
    dao = new TenantDAO(sqlEngine);
  }

  @Test
  void roundTrip() {
    final Tenant tenant = randomTenant();
    assertThat(dao.read(tenant.id())).isEmpty();
    assertThat(dao.create(tenant)).isEqualTo(tenant);
    assertThat(dao.read(tenant.id())).isPresent().contains(tenant);
  }

  @Test
  void list() {
    final List<Tenant> tenants = List.of(randomTenant(), randomTenant(), randomTenant());
    tenants.forEach(dao::create);
    final List<String> ids = tenants.stream().map(Tenant::id).toList();
    assertThat(dao.allTenants()).containsOnlyOnceElementsOf(ids);
  }

  @Test
  void delete() {
    final Tenant tenant = randomTenant();
    assertThat(dao.delete(tenant.id())).isFalse();
    dao.create(tenant);
    assertThat(dao.delete(tenant.id())).isTrue();
  }

  private Tenant randomTenant() {
    return ImmutableTenant.builder()
        .id(UUID.randomUUID().toString())
        .nonce(UUID.randomUUID().toString())
        .key(UUID.randomUUID().toString())
        .uuid(UUID.randomUUID().toString())
        .build();
  }
}