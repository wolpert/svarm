package org.svarm.queue;

import java.util.function.Consumer;

/**
 * The interface Queue.
 */
public interface Queue {

  /**
   * Register.
   *
   * @param messageType the message type
   * @param consumer    the consumer
   */
  void register(final String messageType,
                final Consumer<Message> consumer);

  /**
   * Enqueue message.
   *
   * @param messageType the message type
   * @param payload     the payload
   * @return the message
   */
  Message enqueue(final String messageType,
                  final String payload);

  /**
   * Gets state.
   *
   * @param message the message
   * @return the state
   */
  State getState(final Message message);

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
