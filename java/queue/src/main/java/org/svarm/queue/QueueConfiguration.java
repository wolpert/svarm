package org.svarm.queue;

import org.immutables.value.Value;

/**
 * The interface Queue configuration.
 */
@Value.Immutable
public interface QueueConfiguration {

  /**
   * Wait between polls seconds int.
   *
   * @return the int
   */
  @Value.Default
  default int waitBetweenPollsSeconds() {
    return 15;
  }

  /**
   * Worker threads int.
   *
   * @return the int
   */
  @Value.Default
  default int workerThreads() {
    return 5;
  }

}
