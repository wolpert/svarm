package org.svarm.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * Provides methods to help with shutdown.
 */
public class ShutdownUtility {

  /**
   * Shutdown.
   *
   * @param logger          the logger
   * @param name            the name
   * @param executorService the executor service
   */
  public static void shutdown(final Logger logger, final String name, final ExecutorService executorService) {
    logger.info("{}: Shutting down the executor service", name);
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(15, TimeUnit.SECONDS)) {
        logger.info("{}: Shutting down nicely failed. No longer being nice.", name);
        executorService.shutdownNow().forEach(runnable -> logger.warn("{}: Unable to shutdown {}", name, runnable));
      }
    } catch (InterruptedException e) {
      logger.error("{}: InterruptedException while shutting down executor service", name, e);
    }
    logger.info("{}: Executor service no longer executing messages", name);
  }

}
