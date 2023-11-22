package org.svarm.common.javaclient;

import org.immutables.value.Value;

/**
 * The interface Java client config.
 */
@Value.Immutable
public interface JavaClientConfig {

  /**
   * Test config java client config.
   *
   * @return the java client config
   */
  static JavaClientConfig testConfig() {
    return ImmutableJavaClientConfig.builder()
        .disableSslVerification(true)
        .build();
  }

  /**
   * Default config java client config.
   *
   * @return the java client config
   */
  static JavaClientConfig defaultConfig() {
    return ImmutableJavaClientConfig.builder()
        .build();
  }

  /**
   * Disable ssl verification boolean.
   *
   * @return the boolean
   */
  @Value.Default
  default boolean disableSslVerification() {
    return false;
  }

}
