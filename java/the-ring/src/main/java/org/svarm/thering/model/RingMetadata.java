package org.svarm.thering.model;

import java.util.Set;
import org.immutables.value.Value;

/**
 * Defines a ring structure for participating in a distributed system.
 */
@Value.Immutable
public interface RingMetadata {

  static RingMetadata of(int range, int replicationFactor) {
    return ImmutableRingMetadata.builder()
        .range(range)
        .replicationFactor(replicationFactor)
        .build();
  }

  Integer range();

  Integer replicationFactor();

  @Value.Derived
  default Integer replicationDistance() {
    return range() / replicationFactor();
  }

}
