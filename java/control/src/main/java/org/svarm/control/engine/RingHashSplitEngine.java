package org.svarm.control.engine;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Manages splitting of hashes on a node.
 */
@Singleton
public class RingHashSplitEngine {

  private static final Logger LOGGER = getLogger(RingHashSplitEngine.class); // one for zero.
  private static final Long TOTAL_VALUES = ((long) Integer.MIN_VALUE * -1) + (long) Integer.MAX_VALUE + 1L;


  /**
   * Constructor.
   */
  @Inject
  public RingHashSplitEngine() {
    LOGGER.info("RingHashSplitEngine()");
  }

  /**
   * Returns a list of integers representing the hashes for the space evenly divided. This is really only called
   * once, during creation of a new cluster.
   *
   * @param nodeCount to calculate.
   * @return the list of hashes. This is not immutable or shared. You can muck with it.
   */
  public List<Integer> evenSplitHashes(final int nodeCount) {
    final long additionBlob = TOTAL_VALUES / nodeCount;
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
