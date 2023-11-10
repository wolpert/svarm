package org.svarm.queue;

import java.util.Optional;

/**
 * The interface Queue.
 */
public interface Queue {

  /**
   * Enqueue message.
   *
   * @param messageType the message type
   * @param payload     the payload
   * @return the message if it could be enqueued.
   */
  Optional<Message> enqueue(final String messageType,
                            final String payload);

  /**
   * Gets state.
   *
   * @param message the message
   * @return the state if it is found.
   */
  Optional<State> getState(final Message message);

  /**
   * Clear all.
   */
  void clearAll();

  /**
   * Clear.
   *
   * @param message the message
   */
  void clear(final Message message);

}
