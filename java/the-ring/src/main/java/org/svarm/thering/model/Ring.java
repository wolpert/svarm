package org.svarm.thering.model;

import org.immutables.value.Value;

/**
 * Defines a ring structure for participating in a distributed system.
 */
@Value.Immutable
public interface Ring {

  static Ring of(int range, int replicationFactor) {
    if (range < 1 || replicationFactor < 1) {
      throw new IllegalArgumentException("Range, Replication Factor and Nodes must be greater than zero.");
    } else if (replicationFactor > range) {
      throw new IllegalArgumentException("Replication Factor and Nodes must be less than or equal to Range.");
    }
    return ImmutableRing.builder()
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
