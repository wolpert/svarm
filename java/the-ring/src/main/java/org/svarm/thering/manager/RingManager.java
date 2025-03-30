package org.svarm.thering.manager;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RingManager {
  private static final Logger LOG = LoggerFactory.getLogger(RingManager.class);

  @Inject
  public RingManager() {
    LOG.info("RingManager()");
  }

}
