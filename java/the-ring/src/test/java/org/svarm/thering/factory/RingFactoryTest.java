package org.svarm.thering.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RingFactoryTest {

  @InjectMocks private RingFactory ringFactory;

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

  @ParameterizedTest
  @MethodSource("expectedRangesForReplicationFactor3")
  void expectedRangesForReplicationFactor3(int range, int nodes,
                                           int expectedDistance, int expectedSecondStart, int expectedThirdStart) {
    var ring = ringFactory.generate(3, range, nodes);

    assertThat(ring).isNotNull();
    assertThat(ring.ringMetadata())
        .isNotNull()
        .hasFieldOrPropertyWithValue("range", range)
        .hasFieldOrPropertyWithValue("replicationFactor", 3)
        .hasFieldOrPropertyWithValue("replicationDistance", expectedDistance);
    assertThat(ring.nodes())
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

}