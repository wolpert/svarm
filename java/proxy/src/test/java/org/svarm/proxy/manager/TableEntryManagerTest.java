package org.svarm.proxy.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.codeheadsystems.metrics.Metrics;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.node.api.EntryInfo;
import org.svarm.proxy.engine.NodeRangeResolverEngine;
import org.svarm.proxy.engine.NodeTenantTableEntryServiceEngine;

@ExtendWith(MockitoExtension.class)
class TableEntryManagerTest {
  @Mock private NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine;
  @Mock private NodeRangeResolverEngine nodeRangeResolverEngine;
  @Mock private Clock clock;
  @Mock private Metrics metrics;
  @Mock private ExecutorService nodeServiceExecutor;

  @InjectMocks private TableEntryManager manager;

  @Test
  void largestFirst() {
    final JsonNode nodeRare = mock(JsonNode.class);
    final JsonNode nodeCommon = mock(JsonNode.class);
    final EntryInfo info1 = mock(EntryInfo.class);
    final EntryInfo info2 = mock(EntryInfo.class);
    final EntryInfo info3 = mock(EntryInfo.class);
    final Map<JsonNode, List<EntryInfo>> map = Map.of(
        nodeRare, List.of(info1),
        nodeCommon, List.of(info2, info3)
    );
    final Optional<EntryInfo> result = map.entrySet().stream()
        .sorted(manager::largestListFirst)
        .map(Map.Entry::getValue)
        .map(list -> list.get(0))
        .findFirst();
    assertThat(result).isPresent()
        .contains(info2);
  }
}