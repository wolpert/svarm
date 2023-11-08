package org.svarm.queue.factory;

import java.time.Clock;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.svarm.queue.ImmutableMessage;
import org.svarm.queue.Message;

/**
 * The type Message factory.
 */
@Singleton
public class MessageFactory {

  private final Clock clock;

  /**
   * Instantiates a new Message factory.
   *
   * @param clock the clock
   */
  @Inject
  public MessageFactory(final Clock clock) {
    this.clock = clock;
  }

  /**
   * Create message message.
   *
   * @param messageType the message type
   * @param payload     the payload
   * @return the message
   */
  public Message createMessage(final String messageType, final String payload) {
    return ImmutableMessage.builder()
        .uuid(java.util.UUID.randomUUID())
        .timestamp(clock.instant())
        .messageType(messageType)
        .payload(payload)
        .build();
  }

}
