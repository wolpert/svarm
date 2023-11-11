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

  /**
   * Exception on enqueue fail boolean.
   *
   * @return the boolean
   */
  @Value.Default
  default boolean exceptionOnEnqueueFail() {
    return true;
  }

  /**
   * Queue processor initial delay int for processing the durable queue for entries.
   *
   * @return the int
   */
  @Value.Default
  default int queueProcessorInitialDelay() {
    return 50;
  }

  /**
   * Queue processor interval int for processing the durable queue for entries.
   *
   * @return the int
   */
  @Value.Default
  default int queueProcessorInterval() {
    return 15;
  }

  /**
   * Queue executor min threads int.
   *
   * @return the int
   */
  default int queueExecutorMinThreads() {
    return 10;
  }

  /**
   * Queue executor max threads int.
   *
   * @return the int
   */
  default int queueExecutorMaxThreads() {
    return 20;
  }

  /**
   * Queue executor idle seconds int.
   *
   * @return the int
   */
  default int queueExecutorIdleSeconds() {
    return 30;
  }

}
