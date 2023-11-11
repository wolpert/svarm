package org.svarm.queue;

/**
 * The enum Queue state.
 */
public enum State {

  /**
   * Pending queue state. Used for adding requests to the queue.
   */
  PENDING,
  /**
   * Activate queue state. When the worker is assigned the request, and it is close to running.
   */
  ACTIVATING,
  /**
   * PROCESSING queue state. Used when the work is executing the request.
   */
  PROCESSING

}
