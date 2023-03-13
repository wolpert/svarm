package org.svarm.control.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.api.NodeTenantResource;
import org.svarm.common.config.api.NodeTenantResourceRange;
import org.svarm.control.model.NodeRange;

@ExtendWith(MockitoExtension.class)
class NodeRangeConverterTest {

  private static final String TENANT = "TENANT";
  private static final String TABLE = "table";
  private static final String UUID = "uuid";

  @Mock private NodeRange nodeRange;
  @InjectMocks private NodeRangeConverter converter;

  @Test
  void simpleConversion() {
    mockIt(nodeRange, true);
    final List<NodeTenantResourceRange> result = converter.toNodeTenantResourceRanges(TENANT, TABLE, List.of(nodeRange));
    assertThat(result)
        .isNotNull()
        .hasSize(1);
    final NodeTenantResourceRange nodeTenantResourceRange = result.get(0);
    final NodeTenantResource nodeTenantResource = nodeTenantResourceRange.nodeTenantResource();
    assertThat(nodeTenantResource)
        .hasFieldOrPropertyWithValue("uuid", UUID)
        .extracting("tenantResource")
        .hasFieldOrPropertyWithValue("tenant", TENANT)
        .hasFieldOrPropertyWithValue("resource", TABLE);
    assertThat(nodeTenantResourceRange)
        .extracting("range")
        .hasFieldOrPropertyWithValue("hash", Integer.MIN_VALUE);
  }


  private void mockIt(final NodeRange nodeRange, final boolean ready) {
    when(nodeRange.nodeUuid()).thenReturn(UUID);
    when(nodeRange.hash()).thenReturn(Integer.MIN_VALUE);
    when(nodeRange.status()).thenReturn(NodeRange.STATUS_INIT);
  }

}