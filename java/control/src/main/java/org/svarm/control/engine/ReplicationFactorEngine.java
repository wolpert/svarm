package org.svarm.control.engine;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Manages replication factors for nodes.
 * I feel as though this may as well be static constants? I really doubt there would be any replication factor
 * greater than five.
 */
@Singleton
public class ReplicationFactorEngine {

  private static final Logger LOGGER = getLogger(ReplicationFactorEngine.class); // one for zero.
  private static final Long TOTAL_VALUES = ((long) Integer.MIN_VALUE * -1) + (long) Integer.MAX_VALUE + 1L;


  /**
   * Constructor.
   */
  @Inject
  public ReplicationFactorEngine() {
    LOGGER.info("ReplicationFactorEngine()");
  }

  /**
   * Returns a list of integers representing the hashes for the space evenly divided.
   *
   * @param replicationFactor to calculate.
   * @return the list of hashes. This is not immutable or shared. You can muck with it.
   */
  public List<Integer> evenSplitHashes(final int replicationFactor) {
    final long additionBlob = TOTAL_VALUES / replicationFactor;
    final ArrayList<Integer> result = new ArrayList<>();
    long currentValue = Integer.MIN_VALUE;
    long maxValue = Integer.MAX_VALUE;
    while (currentValue < maxValue) {
      result.add(Math.toIntExact(currentValue)); // high hash
      currentValue += additionBlob;
    }
    return result;
  }

}
