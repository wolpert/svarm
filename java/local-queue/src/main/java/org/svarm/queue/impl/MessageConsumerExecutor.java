package org.svarm.queue.impl;

import static org.svarm.queue.module.QueueModule.QUEUE_PROCESSOR_EXECUTOR;

import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.queue.Message;
import org.svarm.queue.MessageConsumer;
import org.svarm.queue.State;
import org.svarm.queue.dao.MessageDao;

/**
 * The type Message consumer executor.
 */
@Singleton
public class MessageConsumerExecutor implements Managed {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerExecutor.class);

  private final ExecutorService executorService;
  private final MessageDao messageDao;
  private final QueueRegister queueRegister;

  /**
   * Instantiates a new Message consumer executor.
   *
   * @param executorService the executor service
   * @param messageDao      the message dao
   * @param queueRegister   the queue register
   */
  @Inject
  public MessageConsumerExecutor(@Named(QUEUE_PROCESSOR_EXECUTOR) final ExecutorService executorService,
                                 final MessageDao messageDao,
                                 final QueueRegister queueRegister) {
    this.executorService = executorService;
    this.messageDao = messageDao;
    this.queueRegister = queueRegister;
    LOGGER.info("MessageConsumerExecutor({},{},{})", messageDao, executorService, queueRegister);
  }

  /**
   * Enqueue.
   *
   * @param message the message
   */
  public void enqueue(final Message message) {
    LOGGER.trace("enqueue({})", message);
    queueRegister.getConsumer(message.messageType())
        .ifPresentOrElse(
            messageConsumer -> executorService.execute(() -> execute(message, messageConsumer)),
            () -> LOGGER.error("No message for type {}", message.messageType()));
  }

  private void execute(final Message message, final MessageConsumer consumer) {
    LOGGER.trace("execute({},{})", message, consumer);
    try {
      messageDao.updateState(message, State.PROCESSING);
      consumer.accept(message);
    } catch (final Throwable t) {
      LOGGER.error("Error processing message: {}", message, t); // do not die
    } finally {
      messageDao.delete(message);
    }
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("stop()");
    LOGGER.info("Shutting down the executor service");
    if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
      LOGGER.info("Shutting down nicely failed. No longer being nice.");
      executorService.shutdownNow();
    }
  }
}
