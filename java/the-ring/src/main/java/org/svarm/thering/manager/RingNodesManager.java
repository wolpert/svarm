package org.svarm.thering.manager;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.svarm.thering.model.Node;
import org.svarm.thering.model.Ring;
import org.svarm.thering.model.RingNodes;

@Singleton
public class RingNodesManager {

  @Inject
  public RingNodesManager() {

  }

  public RingNodes initialize(final Ring ring,
                              final int nodes) {
    if (nodes <= 0) {
      throw new IllegalArgumentException("Number of nodes must be greater than zero.");
    } else if (nodes > ring.range()) {
      throw new IllegalArgumentException("Number of nodes cannot be greater than the range of the ring.");
    }
    final int rangePerNode = ring.range() / nodes; // Calculate the range for each node
    final Map<Integer, Node> map = IntStream.range(0, nodes)
        .map(l -> l * rangePerNode)
        .mapToObj(Node::of)
        .collect(Collectors.toMap(Node::rangeStart, Function.identity()));
    return RingNodes.of(map);
  }

}
