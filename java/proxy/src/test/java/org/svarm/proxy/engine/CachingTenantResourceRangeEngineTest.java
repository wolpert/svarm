package org.svarm.proxy.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.api.TenantResourceRange;
import org.svarm.common.config.engine.NodeConfigurationEngine;
import org.svarm.common.config.engine.WatchEngine;
import org.svarm.common.config.factory.WatchEngineFactory;
import org.svarm.proxy.ProxyConfiguration;

@ExtendWith(MockitoExtension.class)
class CachingTenantResourceRangeEngineTest {

  private static final String TENANT_RESOURCE_KEY = "tenantResourceKey";
  @Mock private NodeConfigurationEngine nodeConfigurationEngine;
  @Mock private WatchEngineFactory watchEngineFactory;
  @Mock private ProxyConfiguration proxyConfiguration;
  @Mock private TenantResource tenantResource;
  @Mock private WatchEngine watchEngine;
  @Mock private TenantResourceRange tenantResourceRange;


  @InjectMocks private CachingTenantResourceRangeEngine engine;

  @Test
  void generateTest() {
    when(nodeConfigurationEngine.readTenantResourceRange(tenantResource)).thenReturn(Optional.of(tenantResourceRange));
    when(nodeConfigurationEngine.getTenantResourceKey(tenantResource)).thenReturn(TENANT_RESOURCE_KEY);
    when(watchEngineFactory.watchEngine(eq(NodeConfigurationEngine.TENANT_NAMESPACE), eq(TENANT_RESOURCE_KEY), any())).thenReturn(watchEngine);

    assertThat(engine.readTenantResourceRange(tenantResource))
        .contains(tenantResourceRange);
  }

}