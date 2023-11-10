package org.svarm.queue.impl;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.queue.Message;
import org.svarm.queue.Queue;
import org.svarm.queue.QueueConfiguration;
import org.svarm.queue.State;
import org.svarm.queue.dao.MessageDao;
import org.svarm.queue.factory.MessageFactory;

/**
 * The type Queue.
 */
@Singleton
public class QueueImpl implements Queue {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueueImpl.class);

  private final QueueRegister queueRegister;
  private final MessageDao messageDao;
  private final MessageFactory messageFactory;
  private final QueueConfiguration queueConfiguration;

  /**
   * Instantiates a new Queue.
   *
   * @param queueRegister      the queue register
   * @param messageDao         the message dao
   * @param messageFactory     the message factory
   * @param queueConfiguration the queue configuration
   */
  @Inject
  public QueueImpl(final QueueRegister queueRegister,
                   final MessageDao messageDao,
                   final MessageFactory messageFactory,
                   final QueueConfiguration queueConfiguration) {
    this.queueRegister = queueRegister;
    this.messageDao = messageDao;
    this.messageFactory = messageFactory;
    this.queueConfiguration = queueConfiguration;
    LOGGER.info("QueueImpl({}, {},{},{})", queueConfiguration, queueRegister, messageDao, messageFactory);
  }

  @Override
  public Optional<Message> enqueue(final String messageType, final String payload) {
    LOGGER.trace("enqueue({},{})", messageType, payload);
    if (queueRegister.getConsumer(messageType).isEmpty()) {
      LOGGER.warn("No consumer registered for message type: {}", messageType);
      return Optional.empty();
    }
    final Message message = messageFactory.createMessage(messageType, payload);
    try {
      messageDao.store(message, State.PENDING);
      return Optional.of(message);
    } catch (final UnableToExecuteStatementException e) {
      if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
        LOGGER.warn("Message already exists: {}", message);
        final Message existingMessage = messageDao.readByHash(message.hash()) // lookup since the UUID could be different
            .orElseThrow(() -> new IllegalStateException("Message should exist: " + message));
        return Optional.of(existingMessage);
      } else {
        if (queueConfiguration.exceptionOnEnqueueFail()) {
          LOGGER.error("Unable to store message: {}", message, e);
          throw e;
        } else {
          LOGGER.warn("Unable to store message: {}", message, e);
          return Optional.empty();
        }
      }
    }
  }

  @Override
  public Optional<State> getState(final Message message) {
    LOGGER.trace("getState({})", message);
    return messageDao.stateOf(message);
  }

  @Override
  public void clearAll() {
    LOGGER.trace("clearAll()");
    messageDao.deleteAll();
  }

  @Override
  public void clear(final Message message) {
    LOGGER.trace("clear({})", message);
    messageDao.delete(message);
  }
}
