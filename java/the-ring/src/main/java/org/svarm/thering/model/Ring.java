package org.svarm.thering.model;

import java.util.Map;
import org.immutables.value.Value;

/**
 * Defines a ring structure for participating in a distributed system.
 */
@Value.Immutable
public interface Ring {

  static Ring of(RingMetadata ringMetadata, Map<Integer, Node> nodes) {
    return ImmutableRing.builder()
        .ringMetadata(ringMetadata)
        .nodes(nodes)
        .build();
  }

  RingMetadata ringMetadata();

  Map<Integer, Node> nodes();

}
