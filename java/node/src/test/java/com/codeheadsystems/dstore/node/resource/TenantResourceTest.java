package com.codeheadsystems.dstore.node.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.node.api.TenantInfo;
import com.codeheadsystems.dstore.node.converter.TenantInfoConverter;
import com.codeheadsystems.dstore.node.manager.TenantManager;
import com.codeheadsystems.dstore.node.model.Tenant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantResourceTest {

  private static final String TENANT = "tenant";

  @Mock private TenantManager tenantManager;
  @Mock private TenantInfoConverter tenantInfoConverter;
  @Mock private List<String> list;
  @Mock private Tenant tenant;
  @Mock private TenantInfo tenantInfo;

  @InjectMocks private TenantResource resource;

  @Test
  void list() {
    when(tenantManager.tenants()).thenReturn(list);
    assertThat(resource.list()).isEqualTo(list);
  }

  @Test
  void read_withValue() {
    when(tenantManager.get(TENANT)).thenReturn(Optional.of(tenant));
    when(tenantInfoConverter.from(tenant)).thenReturn(tenantInfo);
    assertThat(resource.read(TENANT))
        .isPresent()
        .get()
        .isEqualTo(tenantInfo);
  }

  @Test
  void read_withNoValue() {
    when(tenantManager.get(TENANT)).thenReturn(Optional.empty());
    assertThat(resource.read(TENANT))
        .isEmpty();
  }

  @Test
  void create() {
    when(tenantManager.create(TENANT)).thenReturn(tenant);
    when(tenantInfoConverter.from(tenant)).thenReturn(tenantInfo);
    assertThat(resource.create(TENANT))
        .isEqualTo(tenantInfo);
  }

  @Test
  void delete_found() {
    when(tenantManager.delete(TENANT)).thenReturn(true);
    assertThat(resource.delete(TENANT))
        .hasFieldOrPropertyWithValue("status", 204);
  }

  @Test
  void delete_notFound() {
    when(tenantManager.delete(TENANT)).thenReturn(false);
    assertThat(resource.delete(TENANT))
        .hasFieldOrPropertyWithValue("status", 404);
  }

}