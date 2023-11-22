package org.svarm.common.javaclient;

import dagger.Module;
import dagger.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import javax.inject.Singleton;

/**
 * If you don't have your own meter registry, you can use this. But really you need your own.
 */
@Module
public class StandaloneMeterRegistry {

  /**
   * Instantiates a new Standalone meter registry.
   */
  public StandaloneMeterRegistry() {
  }

  /**
   * Default registry.
   *
   * @return the value.
   */
  @Provides
  @Singleton
  MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }

}
