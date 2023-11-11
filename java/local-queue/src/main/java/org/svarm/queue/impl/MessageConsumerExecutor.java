package org.svarm.queue.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.queue.Message;

/**
 * The type Message consumer executor.
 */
@Singleton
public class MessageConsumerExecutor {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerExecutor.class);

  /**
   * Instantiates a new Message consumer executor.
   */
  @Inject
  public MessageConsumerExecutor() {
    LOGGER.info("MessageConsumerExecutor()");
  }

  /**
   * Enqueue.
   *
   * @param message the message
   */
  public void enqueue(final Message message) {
    LOGGER.trace("enqueue({})", message);
  }
}
