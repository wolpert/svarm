package org.svarm.queue.factory;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
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
  private final HashFunction hashFunction;

  /**
   * Instantiates a new Message factory.
   *
   * @param clock        the clock
   * @param hashFunction the hash function
   */
  @Inject
  public MessageFactory(final Clock clock,
                        final HashFunction hashFunction) {
    this.clock = clock;
    this.hashFunction = hashFunction;
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
        .hash(hashFunction.hashString(payload, Charsets.UTF_8).toString())
        .build();
  }

}
