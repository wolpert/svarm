package org.svarm.thering.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.svarm.thering.model.Ring;

class RingNodesManagerTest {

  private RingNodesManager ringNodesManager;

  static Stream<Arguments> expectedRangesForReplicationFactor3() {
    // This method provides test cases for different ranges and nodes
    return Stream.of(
        Arguments.of(9, 3, 3, 3, 6),
        Arguments.of(10, 3, 3, 3, 6),
        Arguments.of(11, 3, 3, 3, 6),
        Arguments.of(12, 3, 4, 4, 8),
        Arguments.of(12, 4, 4, 3, 6)
    );
  }

  @BeforeEach
  void setUp() {
    ringNodesManager = new RingNodesManager();
  }

  @ParameterizedTest
  @MethodSource("expectedRangesForReplicationFactor3")
  void expectedRangesForReplicationFactor3(int range, int nodes,
                                           int expectedDistance, int expectedSecondStart, int expectedThirdStart) {
    var ring = Ring.of(range, 3);
    var ringNodes = ringNodesManager.initialize(ring, nodes);

    assertThat(ring)
        .isNotNull()
        .hasFieldOrPropertyWithValue("range", range)
        .hasFieldOrPropertyWithValue("replicationFactor", 3)
        .hasFieldOrPropertyWithValue("replicationDistance", expectedDistance);
    assertThat(ringNodes).isNotNull();
    assertThat(ringNodes.nodesByRangeStart())
        .isNotNull()
        .hasSize(nodes)
        .containsKeys(0, expectedSecondStart, expectedThirdStart)
        .hasEntrySatisfying(0, node -> {
          // Check properties of the node
          assertThat(node).isNotNull();
          assertThat(node.rangeStart()).isEqualTo(0);
          // Add more assertions as needed for the Node properties
        })
        .hasEntrySatisfying(expectedSecondStart, node -> {
          // Check properties of the node
          assertThat(node).isNotNull();
          assertThat(node.rangeStart()).isEqualTo(expectedSecondStart);
          // Add more assertions as needed for the Node properties
        })
        .hasEntrySatisfying(expectedThirdStart, node -> {
          // Check properties of the node
          assertThat(node).isNotNull();
          assertThat(node.rangeStart()).isEqualTo(expectedThirdStart);
          // Add more assertions as needed for the Node properties
        });
  }

  @Test
  void testNegativeNodeCount() {
    var ring = Ring.of(10, 3);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ringNodesManager.initialize(ring, -1));
  }

  @Test
  void testZeroNodeCount() {
    var ring = Ring.of(10, 3);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ringNodesManager.initialize(ring, 0))
        .withMessage("Number of nodes must be greater than zero.");
  }

  @Test
  void testNodeCountGreaterThanRange() {
    var ring = Ring.of(10, 3);
    // Test case where the number of nodes is greater than the range
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ringNodesManager.initialize(ring, 11))
        .withMessage("Number of nodes cannot be greater than the range of the ring.");
  }

}