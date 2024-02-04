package org.svarm.featureflag.manager.impl;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.config.accessor.EtcdAccessor;
import org.svarm.featureflag.manager.FeatureLookupManager;

/**
 * The type Etcd feature lookup manager.
 */
@Singleton
public class EtcdFeatureLookupManager implements FeatureLookupManager {

  /**
   * The constant NAMESPACE.
   */
  public static final String NAMESPACE = "feature_flag";

  private static final Logger LOGGER = LoggerFactory.getLogger(EtcdFeatureLookupManager.class);

  private final EtcdAccessor accessor;

  /**
   * Instantiates a new Etcd feature lookup manager.
   *
   * @param accessor the accessor
   */
  @Inject
  public EtcdFeatureLookupManager(final EtcdAccessor accessor) {
    this.accessor = accessor;
    LOGGER.info("EtcdFeatureLookupManager({})", accessor);
  }

  @Override
  public Optional<Double> lookupPercentage(final String featureId) {
    LOGGER.trace("lookupPercentage({})", featureId);
    return accessor.get(NAMESPACE, featureId)
        .map(Double::parseDouble);
  }

  @Override
  public boolean setPercentage(final String featureId, final double percentage) {
    LOGGER.trace("setPercentage({}, {})", featureId, percentage);
    accessor.put(NAMESPACE, featureId, String.valueOf(percentage));
    return true;
  }

  @Override
  public void deletePercentage(final String featureId) {
    LOGGER.trace("deletePercentage({})", featureId);
    accessor.delete(NAMESPACE, featureId);
  }
}
