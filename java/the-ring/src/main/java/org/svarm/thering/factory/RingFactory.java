package org.svarm.thering.factory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.thering.model.Node;
import org.svarm.thering.model.Ring;
import org.svarm.thering.model.RingMetadata;

@Singleton
public class RingFactory {

  private static final Logger log = LoggerFactory.getLogger(RingFactory.class);

  @Inject
  public RingFactory() {
    log.info("RingFactory()");
  }

  /**
   * Generates a ring structure for a distributed system.
   * All values must be non-zero. Both replication factory and nodes must be less than range,
   * and replication factory cannot be greater than nodes.
   *
   * @param replicationFactor the number of replicas for each node
   * @param range             the range of values for the ring
   * @param nodes             the number of nodes in the ring
   * @return a Ring object representing the generated ring
   */
  public Ring generate(final int replicationFactor, final int range, final int nodes) {
    log.info("generate({},{},{})", replicationFactor, range, nodes);
    if (range < 1 || replicationFactor < 1 || nodes < 1) {
      throw new IllegalArgumentException("Range, Replication Factor and Nodes must be greater than zero.");
    } else if (replicationFactor > range || nodes > range) {
      throw new IllegalArgumentException("Replication Factor and Nodes must be less than or equal to Range.");
    } else if (replicationFactor > nodes) {
      throw new IllegalArgumentException("Replication Factor cannot be greater than Nodes.");
    }
    final RingMetadata ringMetadata = RingMetadata.of(range, replicationFactor);
    final int rangePerNode = range / nodes; // Calculate the range for each node
    final Map<Integer, Node> nodeMap = IntStream.range(0, nodes)
        .map(l -> l * rangePerNode)
        .mapToObj(Node::of)
        .collect(Collectors.toMap(Node::rangeStart, Function.identity()));
    return Ring.of(ringMetadata, nodeMap);
  }

}
