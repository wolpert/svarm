package org.svarm.featureflag.manager;

import java.util.Optional;

/**
 * The interface Feature lookup manager.
 */
@FunctionalInterface
public interface FeatureLookupManager {

  /**
   * Lookup feature.
   *
   * @param featureId the feature id
   * @return the feature percentage dialed up.
   */
  Optional<Double> lookupPercentage(String featureId);

}
