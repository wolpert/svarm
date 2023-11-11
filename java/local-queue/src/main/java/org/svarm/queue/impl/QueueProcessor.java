package org.svarm.queue.impl;

import static org.svarm.queue.module.QueueModule.QUEUE_PROCESSOR_SCHEDULER;

import com.codeheadsystems.metrics.Metrics;
import io.dropwizard.lifecycle.Managed;
import io.micrometer.core.instrument.Tags;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.queue.QueueConfiguration;
import org.svarm.queue.State;
import org.svarm.queue.dao.MessageDao;
import org.svarm.queue.factory.QueueConfigurationFactory;

/**
 * The Queue Processor. Reads the queues and creates workers for the messages.
 */
@Singleton
public class QueueProcessor implements Managed {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueueProcessor.class);

  private final MessageDao dao;
  private final QueueConfiguration queueConfiguration;
  private final MessageConsumerExecutor messageConsumerExecutor;
  private final ScheduledExecutorService scheduledExecutorService;
  private final Metrics metrics;
  private ScheduledFuture<?> scheduler;

  /**
   * Instantiates a new Queue processor.
   *
   * @param dao                       the dao
   * @param queueConfigurationFactory the queue configuration factory
   * @param messageConsumerExecutor   the message consumer executor
   * @param scheduledExecutorService  the scheduled executor service
   * @param metrics                   the metrics
   */
  @Inject
  public QueueProcessor(final MessageDao dao,
                        final QueueConfigurationFactory queueConfigurationFactory,
                        final MessageConsumerExecutor messageConsumerExecutor,
                        @Named(QUEUE_PROCESSOR_SCHEDULER) final ScheduledExecutorService scheduledExecutorService,
                        final Metrics metrics) {
    this.dao = dao;
    this.queueConfiguration = queueConfigurationFactory.queueConfiguration();
    this.messageConsumerExecutor = messageConsumerExecutor;
    this.scheduledExecutorService = scheduledExecutorService;
    this.metrics = metrics;
    LOGGER.info("QueueProcessor({},{},{})", dao, queueConfiguration, messageConsumerExecutor);
  }

  @Override
  public void start() {
    LOGGER.info("start()");
    synchronized (scheduledExecutorService) {
      if (scheduler == null) {
        LOGGER.info("Resetting existing messages to pending state");
        resetState(State.ACTIVATING);
        resetState(State.PROCESSING);
        LOGGER.info("Starting the scheduler");
        scheduler = scheduledExecutorService.scheduleAtFixedRate(this::processPendingQueue,
            queueConfiguration.queueProcessorInitialDelay(),
            queueConfiguration.queueProcessorInterval(),
            TimeUnit.SECONDS);
      }
    }
    LOGGER.info("Queue accepting messages");
  }

  private void resetState(final State state) {
    dao.forState(state).forEach(message -> {
      LOGGER.info("Resetting {} message to PENDING: {}", state, message);
      dao.updateState(message, State.PENDING);
    });
  }

  /**
   * Process pending queue.
   */
  public void processPendingQueue() {
    LOGGER.trace("processPendingQueue()");
    metrics.time("QueueProcessor.processPendingQueue", () -> {
      dao.forState(State.PENDING).forEach(message -> {
        LOGGER.trace("Processing message {}", message);
        dao.updateState(message, State.ACTIVATING);
        messageConsumerExecutor.enqueue(message);
      });
      return null;
    });
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("stop()");
    synchronized (scheduledExecutorService) {
      if (scheduler != null) {
        LOGGER.info("Shutting down the scheduler");
        scheduler.cancel(true);
        scheduler = null;
        LOGGER.info("Shutting down the scheduler service");
        scheduledExecutorService.shutdown();
        if (!scheduledExecutorService.awaitTermination(15, TimeUnit.SECONDS)) {
          LOGGER.info("Shutting down nicely failed. No longer being nice.");
          scheduledExecutorService.shutdownNow();
        }
      }
    }
    LOGGER.info("Queue no longer accepting messages");
  }
}
