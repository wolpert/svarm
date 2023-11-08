package org.svarm.queue.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Queue worker.
 */
public class QueueWorker implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueueWorker.class);

  /**
   * Instantiates a new Queue worker.
   */
  public QueueWorker() {
    LOGGER.info("QueueWorker()");
  }

  @Override
  public void run() {
    LOGGER.trace("run()");
  }
}
