package org.svarm.queue.factory;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.time.Clock;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.queue.ImmutableMessage;
import org.svarm.queue.Message;

/**
 * The type Message factory.
 */
@Singleton
public class MessageFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageFactory.class);
  private final Clock clock;
  private final HashFunction hashFunction;

  /**
   * Instantiates a new Message factory.
   *
   * @param clock the clock
   */
  @Inject
  public MessageFactory(final Clock clock) {
    this.clock = clock;
    this.hashFunction = Hashing.murmur3_128();
    LOGGER.info("MessageFactory({},{})", clock, hashFunction);
  }

  /**
   * Create message message.
   *
   * @param messageType the message type
   * @param payload     the payload
   * @return the message
   */
  public Message createMessage(final String messageType,
                               final String payload) {
    LOGGER.trace("createMessage({},{})", messageType, payload);
    return ImmutableMessage.builder()
        .uuid(java.util.UUID.randomUUID().toString())
        .timestamp(clock.instant().toEpochMilli())
        .messageType(messageType)
        .payload(payload)
        .hash(hashFunction.hashString(payload, Charsets.UTF_8).toString())
        .build();
  }

}
