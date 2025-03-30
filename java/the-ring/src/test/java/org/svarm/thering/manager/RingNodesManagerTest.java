package org.svarm.thering.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.thering.model.ImmutableNode;
import org.svarm.thering.model.Node;
import org.svarm.thering.model.Ring;
import org.svarm.thering.model.RingNodes;

@ExtendWith(MockitoExtension.class)
class RingNodesManagerTest {

  @Mock private Ring ring;

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

  @Test
  void testNodeReplace() {
    final Node node1 = Node.of(1,1);
    final Node node2 = Node.of(2,2);
    final Node node3 = Node.of(3,3);
    final Node node4 = Node.of(4,4);
    final Map<Integer, Node> nodeMap = Map.of(1, node1, 2, node2);
    final Node[] nodes = new Node[] {node3, node4}; // Array of nodes to be replaced

    var result = ringNodesManager.nodeReplace(nodeMap, node2, nodes);
    assertThat(result)
        .hasSize(3)
        .containsKeys(1,3,4)
        .containsValues(node1, node3, node4);
  }

  @Test
  void testSplitNode() {
    final int rangeStart = 0;
    final int rangeEnd = 10;
    final int midpoint = 5;
    final Node node = ImmutableNode.builder()
        .id("node1").rangeStart(rangeStart).rangeEnd(rangeEnd).build();
    when(ring.midpoint(rangeStart, rangeEnd)).thenReturn(Optional.of(midpoint));
    var result = ringNodesManager.splitNode(ring, node);
    assertThat(result).isPresent();
    var nodes = result.get();
    assertThat(nodes).hasSize(2);
    assertThat(nodes[0])
        .hasFieldOrPropertyWithValue("id", node.id())
        .hasFieldOrPropertyWithValue("rangeStart", rangeStart)
        .hasFieldOrPropertyWithValue("rangeEnd", midpoint);
    assertThat(nodes[1])
        .hasFieldOrPropertyWithValue("rangeStart", midpoint)
        .hasFieldOrPropertyWithValue("rangeEnd", rangeEnd)
        .extracting("id")
        .isNotEqualTo(node.id());
  }

  @Test
  void testSplitNode_noMidpoint() {
    final int rangeStart = 0;
    final int rangeEnd = 10;
    final Node node = ImmutableNode.builder()
        .id("node1").rangeStart(rangeStart).rangeEnd(rangeEnd).build();
    when(ring.midpoint(rangeStart, rangeEnd)).thenReturn(Optional.empty());
    var result = ringNodesManager.splitNode(ring, node);
    assertThat(result).isNotPresent();
  }

  @Test
  void testIncrement() {
    final int range = 11;
    final Node node1 = Node.of(0, 5);
    final Node node2 = Node.of(5, 1);
    final Map<Integer, Node> nodeMap = Map.of(0, node1, 5, node2);
    final RingNodes ringNodes = RingNodes.of(nodeMap);
    final Ring ring = Ring.of(range, 3);

    var result = ringNodesManager.increment(ring, ringNodes);

    assertThat(result).isPresent();
    var newRingNodes = result.get();
    assertThat(newRingNodes.nodesByRangeStart())
        .hasSize(3)
        .containsKeys(0, 5, 8);
  }

}