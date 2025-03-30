package org.svarm.thering.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.thering.model.ImmutableNode;
import org.svarm.thering.model.ImmutableRingNodes;
import org.svarm.thering.model.Node;
import org.svarm.thering.model.Ring;
import org.svarm.thering.model.RingNodes;

@Singleton
public class RingNodesManager {

  private static final Logger log = LoggerFactory.getLogger(RingNodesManager.class);

  @Inject
  public RingNodesManager() {
    log.info("RingNodesManager()");
  }

  public RingNodes initialize(final Ring ring,
                              final int nodes) {
    log.trace("initialize({},{})", ring, nodes);
    if (nodes <= 0) {
      throw new IllegalArgumentException("Number of nodes must be greater than zero.");
    } else if (nodes > ring.range()) {
      throw new IllegalArgumentException("Number of nodes cannot be greater than the range of the ring.");
    }
    final int rangePerNode = ring.range() / nodes; // Calculate the range for each node
    final Map<Integer, Node> map = IntStream.range(0, nodes)
        .map(l -> l * rangePerNode)
        .mapToObj(l -> Node.of(l, l + rangePerNode)) // Create a node for each range
        .collect(Collectors.toMap(Node::rangeStart, Function.identity()));
    return RingNodes.of(map);
  }

  /**
   * Basically find the node with the most space and half it.
   *
   * @param ring      the ring to which the nodes belong
   * @param ringNodes the current state of nodes in the ring
   * @return a new RingNodes object with the updated nodes
   */
  public Optional<RingNodes> increment(final Ring ring,
                                       final RingNodes ringNodes) {
    log.trace("increment({},{})", ring, ringNodes);
    final Optional<Node> toHalf = ringNodes.nodesByRangeStart().values()
        .stream()
        .map(node -> new NodeSizeTuple(node, ring))
        .max((entry1, entry2) -> Integer.compare(entry1.size, entry2.size))
        .map(NodeSizeTuple::node); // Get the node itself
    if (toHalf.isPresent()) {
      final Node nodeToRemove = toHalf.get();
      return splitNode(ring, nodeToRemove)
          .map(nodes -> nodeReplace(ringNodes.nodesByRangeStart(), nodeToRemove, nodes))
          .map(map -> ImmutableRingNodes.copyOf(ringNodes).withNodesByRangeStart(map));
    } else {
      log.warn("Unable to find a node to half");
      return Optional.empty();
    }
  }

  public Map<Integer, Node> nodeReplace(final Map<Integer, Node> integerNodeMap,
                                        final Node nodeToRemove,
                                        final Node[] nodes) {
    log.trace("nodeReplace({}, {}, {})", integerNodeMap, nodeToRemove, nodes);
    final HashMap<Integer, Node> newNodesByRangeStart = new HashMap<>(integerNodeMap);
    newNodesByRangeStart.remove(nodeToRemove.rangeStart());
    for (final Node node : nodes) {
      newNodesByRangeStart.put(node.rangeStart(), node);
    }
    return newNodesByRangeStart;
  }

  public Optional<Node[]> splitNode(final Ring ring, final Node node) {
    int rangeStart = node.rangeStart();
    int rangeEnd = node.rangeEnd();
    return ring.midpoint(rangeStart, rangeEnd)
        .map(midpoint -> {
          // reuse node1.
          final Node node1 = ImmutableNode.copyOf(node).withRangeEnd(midpoint);
          final Node node2 = Node.of(midpoint, rangeEnd);
          return new Node[]{node1, node2};
        });
  }

  record NodeSizeTuple(int size, Node node) {
    public NodeSizeTuple(Node node, Ring ring) {
      this(
          // Calculate the size of the node's range
          (node.rangeEnd() >= node.rangeStart() ?
              node.rangeEnd() - node.rangeStart() :
              ring.range() - node.rangeStart() + node.rangeEnd()),
          node);
    }
  }
}
