package org.svarm.queue.impl;

import io.dropwizard.lifecycle.Managed;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.queue.QueueConfiguration;
import org.svarm.queue.dao.MessageDao;
import org.svarm.queue.factory.QueueConfigurationFactory;

/**
 * The type Queue worker.
 */
@Singleton
public class QueueProcessor implements Managed {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueueProcessor.class);

  private final MessageDao dao;
  private final QueueRegister queueRegister;
  private final QueueConfiguration queueConfiguration;

  /**
   * Instantiates a new Queue processor.
   *
   * @param dao                       the dao
   * @param queueRegister             the queue register
   * @param queueConfigurationFactory the queue configuration factory
   */
  @Inject
  public QueueProcessor(final MessageDao dao,
                        final QueueRegister queueRegister,
                        final QueueConfigurationFactory queueConfigurationFactory) {
    this.dao = dao;
    this.queueRegister = queueRegister;
    this.queueConfiguration = queueConfigurationFactory.queueConfiguration();
    LOGGER.info("QueueProcessor({},{},{})", dao, queueRegister, queueConfiguration);
  }

  @Override
  public void start() throws Exception {
    LOGGER.info("start()");
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("stop()");
  }
}
