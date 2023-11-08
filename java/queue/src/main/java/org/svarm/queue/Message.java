package org.svarm.queue;

import java.time.Instant;
import java.util.UUID;
import org.immutables.value.Value;

/**
 * The interface Message.
 */
@Value.Immutable
public interface Message {

  /**
   * Uuid uuid.
   *
   * @return the uuid
   */
  UUID uuid();

  /**
   * Created instant.
   *
   * @return the instant
   */
  Instant timestamp();

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
