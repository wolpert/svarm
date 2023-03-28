package org.svarm.server.initializer;

/**
 * Interface for all initializers.
 */
@FunctionalInterface
public interface Initializer {

  /**
   * Does the initialization.
   */
  void initialize();

}
