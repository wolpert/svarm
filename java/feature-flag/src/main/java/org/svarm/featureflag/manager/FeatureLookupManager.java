package org.svarm.featureflag.manager;

import java.util.Optional;

/**
 * The interface Enablement lookup manager.
 */
public interface FeatureLookupManager {

  /**
   * Lookup feature.
   *
   * @param featureId the feature id
   * @return the feature percentage dialed up.
   */
  Optional<Double> lookupPercentage(String featureId);

  /**
   * Sets percentage.
   *
   * @param featureId  the feature id
   * @param percentage the percentage
   * @return the percentage
   */
  boolean setPercentage(String featureId, double percentage);

  /**
   * Delete percentage boolean.
   *
   * @param featureId the feature id
   */
  void deletePercentage(String featureId);

}
