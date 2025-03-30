package org.svarm.thering.model;

import java.util.UUID;
import org.immutables.value.Value;

@Value.Immutable
public interface Node {

  static Node of(Integer rangeStart, Integer rangeEnd) {
    return ImmutableNode.builder()
        .rangeStart(rangeStart)
        .rangeEnd(rangeEnd)
        .build();
  }

  @Value.Default
  default String id() {
    return UUID.randomUUID().toString();
  }

  /**
   * Start value for the range, inclusive.
   *
   * @return the starting value for the range
   */
  Integer rangeStart();

  /**
   * End value for the range, exclusive.
   *
   * @return the ending value for the range
   */
  Integer rangeEnd();

}
