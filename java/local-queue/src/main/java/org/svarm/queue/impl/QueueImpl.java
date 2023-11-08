package org.svarm.queue.impl;

import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.queue.Message;
import org.svarm.queue.Queue;
import org.svarm.queue.State;

/**
 * The type Queue.
 */
@Singleton
public class QueueImpl implements Queue {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueueImpl.class);

  /**
   * Instantiates a new Queue.
   */
  @Inject
  public QueueImpl() {
    LOGGER.info("QueueImpl()");
  }

  @Override
  public void register(final String messageType, final Consumer<Message> consumer) {
    LOGGER.trace("register({})", messageType);
  }

  @Override
  public Message enqueue(final String messageType, final String payload) {
    LOGGER.trace("enqueue({},{})", messageType, payload);
    return null;
  }

  @Override
  public State getState(final Message message) {
    LOGGER.trace("getState({})", message);
    return null;
  }

  @Override
  public void clearAll() {
    LOGGER.trace("clearAll()");
  }

  @Override
  public void clear(final Message message) {
    LOGGER.trace("clear({})", message);
  }
}
