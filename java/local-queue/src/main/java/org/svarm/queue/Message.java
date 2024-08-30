package org.svarm.queue;

import org.immutables.value.Value;

/**
 * The interface Message.
 */
@Value.Immutable
public interface Message {

  /**
   * Hash string for deduping.
   *
   * @return the string
   */
  String hash();

  /**
   * Created instant.
   *
   * @return the instant
   */
  long timestamp();

  /**
   * Message type string.
   *
   * @return the string
   */
  String messageType();

  /**
   * Payload string.
   *
   * @return the string
   */
  String payload();

}
