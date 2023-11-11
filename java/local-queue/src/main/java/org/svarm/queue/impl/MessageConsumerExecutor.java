package org.svarm.queue.impl;

import static org.svarm.queue.module.QueueModule.QUEUE_PROCESSOR_EXECUTOR;

import com.codeheadsystems.metrics.Metrics;
import io.dropwizard.lifecycle.Managed;
import io.micrometer.core.instrument.Tags;
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
  private final Metrics metrics;

  /**
   * Instantiates a new Message consumer executor.
   *
   * @param executorService the executor service
   * @param messageDao      the message dao
   * @param queueRegister   the queue register
   * @param metrics         the metrics
   */
  @Inject
  public MessageConsumerExecutor(@Named(QUEUE_PROCESSOR_EXECUTOR) final ExecutorService executorService,
                                 final MessageDao messageDao,
                                 final QueueRegister queueRegister, final Metrics metrics) {
    this.executorService = executorService;
    this.messageDao = messageDao;
    this.queueRegister = queueRegister;
    this.metrics = metrics;
    LOGGER.info("MessageConsumerExecutor({},{},{})", messageDao, executorService, queueRegister);
  }

  /**
   * Enqueue.
   *
   * @param message the message
   */
  public void enqueue(final Message message) {
    LOGGER.trace("enqueue({})", message);
    final String messageType = message.messageType();
    metrics.time("MessageConsumerExecutor.enqueue", Tags.of("messageType", messageType), () -> {
      queueRegister.getConsumer(messageType)
          .ifPresentOrElse(
              messageConsumer -> executorService.execute(() -> execute(message, messageConsumer)),
              () -> {
                LOGGER.error("No message for type {}", message.messageType());
                messageDao.delete(message);
              });
      return null;
    });
  }

  private void execute(final Message message, final MessageConsumer consumer) {
    LOGGER.trace("execute({},{})", message, consumer);
    try {
      metrics.time("MessageConsumerExecutor.enqueue", Tags.of("messageType", message.messageType()), () -> {
        messageDao.updateState(message, State.PROCESSING);
        consumer.accept(message);
        return null;
      });
    } catch (final Throwable t) {
      LOGGER.error("Error processing message: {}", message, t); // do not die
    } finally {
      messageDao.delete(message);
    }
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("Executor service enabled to start executing messages");
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("stop()");
    LOGGER.info("Shutting down the executor service");
    executorService.shutdown();
    if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
      LOGGER.info("Shutting down nicely failed. No longer being nice.");
      executorService.shutdownNow().forEach(runnable -> LOGGER.warn("Unable to shutdown {}", runnable));
    }
    LOGGER.info("Executor service no longer executing messages");
  }
}
