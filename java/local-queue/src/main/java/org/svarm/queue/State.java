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
  ACTIVATE,
  /**
   * Running queue state. Used when the work is executing the request.
   */
  RUNNING,
  /**
   * Deactivate queue state. When the request is pending, cleaning up.
   */
  DEACTIVATE,
  /**
   * Complete queue state. When the request is complete, and ready to delete.
   */
  COMPLETE

}
