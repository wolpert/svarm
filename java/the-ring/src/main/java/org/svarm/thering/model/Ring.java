package org.svarm.thering.model;

import java.util.Optional;
import java.util.UUID;
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

  @Value.Derived
  default String id() {
    return UUID.randomUUID().toString();
  }

  @Value.Default
  default Integer range() {
    return Integer.MAX_VALUE;
  }

  @Value.Default
  default Integer replicationFactor() {
    return 3;
  }

  @Value.Derived
  default Integer replicationDistance() {
    return range() / replicationFactor();
  }

  default Optional<Integer> midpoint(final Integer start,
                                     final Integer end) {
    if (start.equals(end) ||
        (end > start && (end - start) == 1) ||
        (end < start && (range() + end - start) == 1)) {
      return Optional.empty(); // Invalid range
    }
    if (start < end) {
      return Optional.of(start + ((end - start) / 2));
    } else {
      // In a ring, if the end is less than the start, we need to wrap around.
      // Calculate the midpoint considering the wrap around.
      long range = range();
      long delta = (range - start + end) / 2;
      long midpoint = start + delta;
      if (midpoint >= range) { // we looped
        midpoint -= range;
      }
      return Optional.of(Long.valueOf(midpoint).intValue());
    }

  }
}
