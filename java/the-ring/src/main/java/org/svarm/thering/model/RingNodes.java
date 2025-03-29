package org.svarm.thering.model;

import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
public interface RingNodes {

  static RingNodes of(Map<Integer, Node> nodesByRangeStart) {
    return ImmutableRingNodes.builder()
        .putAllNodesByRangeStart(nodesByRangeStart)
        .build();
  }

  Map<Integer, Node> nodesByRangeStart();

}
