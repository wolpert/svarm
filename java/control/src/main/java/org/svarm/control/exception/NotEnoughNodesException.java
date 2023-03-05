package org.svarm.control.exception;

/**
 * Thrown when there are not enough nodes.
 */
public class NotEnoughNodesException extends RuntimeException{

  /**
   * Main constructor.
   *
   * @param message to send.
   */
  public NotEnoughNodesException(final String message) {
    super(message);
  }
}
