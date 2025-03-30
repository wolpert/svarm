package org.svarm.thering.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RingTest {

  static Stream<Arguments> validOfParameters() {
    return Stream.of(
        // Test cases for valid inputs
        Arguments.of(10, 2, false), // Valid case: range 10, replication factor 2
        Arguments.of(12, 3, false), // Valid case: range 12, replication factor 3
        Arguments.of(15, 5, false), // Valid case: range 15, replication factor 5
        // Test cases for invalid inputs
        Arguments.of(0, 0, true), // Invalid case: replication factor greater than range
        Arguments.of(10, 0, true),  // Invalid case: replication factor 0
        Arguments.of(10, -1, true), // Invalid case: negative replication factor
        Arguments.of(5, 6, true)    // Invalid case: replication factor greater than range
    );

  }

  static Stream<Arguments> midpointTestValues() {
    return Stream.of(
        Arguments.of(10, 0, 5, Optional.of(2)),
        Arguments.of(10, 5, 0, Optional.of(7)),
        Arguments.of(10, 5, 5, Optional.empty()),
        Arguments.of(10, 5, 6, Optional.empty()),
        Arguments.of(10, 9, 1, Optional.of(0)),
        Arguments.of(10, 1, 9, Optional.of(5)),
        Arguments.of(10, 9, 0, Optional.empty()),
        Arguments.of(10, 0, 9, Optional.of(4))
    );
  }

  @ParameterizedTest
  @MethodSource("validOfParameters")
  void test_validOf(int range, int replicationFactor, boolean exceptionThrown) {
    if (exceptionThrown) {
      assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> Ring.of(range, replicationFactor));
    } else {
      assertThat(Ring.of(range, replicationFactor))
          .isNotNull()
          .hasFieldOrPropertyWithValue("range", range)
          .hasFieldOrPropertyWithValue("replicationFactor", replicationFactor)
          .hasFieldOrPropertyWithValue("replicationDistance", range / replicationFactor);
    }
  }

  @ParameterizedTest
  @MethodSource("midpointTestValues")
  void testMidpoint(int ringsize, int start, int end, Optional<Integer> expectedValue){
    var ring = Ring.of(ringsize, 3);
    var result = ring.midpoint(start, end);
    expectedValue.ifPresentOrElse(
        value -> assertThat(result).isPresent().contains(value),
        () -> assertThat(result).isEmpty());
  }

  static Stream<Arguments> replicatedHashesTestValues() {
    // This method provides test cases for different ranges and hashes
    return Stream.of(
        Arguments.of(10, 3, 0, Set.of(0, 3, 6)),
        Arguments.of(10, 3, 1, Set.of(1, 4, 7)),
        Arguments.of(10, 3, 2, Set.of(2, 5, 8)),
        Arguments.of(10, 3, 3, Set.of(3, 6, 9)),
        Arguments.of(10, 3, 4, Set.of(4, 7, 0)),
        Arguments.of(10, 3, 5, Set.of(5, 8, 1)),
        Arguments.of(12, 4, 0, Set.of(0, 3, 6, 9)),
        Arguments.of(12, 4, 1, Set.of(1, 4, 7, 10)),
        Arguments.of(12, 4, 2, Set.of(2, 5, 8, 11))
    );
  }

  @ParameterizedTest
  @MethodSource("replicatedHashesTestValues")
  void testReplicationHash(int range, int replicationFactory, int hash, Set<Integer> expectation){
    var ring = Ring.of(range, replicationFactory);
    var result = ring.replicatedHashes(hash);
    assertThat(result)
        .isNotNull()
        .hasSize(expectation.size())
        .containsExactlyInAnyOrderElementsOf(expectation);
  }

}