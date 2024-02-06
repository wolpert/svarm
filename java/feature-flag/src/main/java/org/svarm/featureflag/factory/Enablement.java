package org.svarm.featureflag.factory;

/**
 * Enablement interface to vet if something is enabled for a given discriminator.
 */
@FunctionalInterface
public interface Enablement {

  /**
   * Is the feature enabled for the given discriminator.
   *
   * @param discriminator we want to check.
   * @return boolean if enabled or not.
   */
  boolean enabled(String discriminator);

}
