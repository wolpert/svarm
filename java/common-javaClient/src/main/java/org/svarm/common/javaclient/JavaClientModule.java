/*
 * Copyright (c) 2023. Ned Wolpert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.svarm.common.javaclient;

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import feign.FeignException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Module for the java client.
 */
@Module(includes = {JavaClientModule.Binder.class})
public class JavaClientModule {

  /**
   * The default retry policy.
   *
   * @param meterRegistry meter registry.
   * @return the retry.
   */
  @Provides
  @Singleton
  @Named("DEFAULT")
  public Retry retry(final MeterRegistry meterRegistry) {
    final RetryConfig config = RetryConfig.custom()
        .maxAttempts(3)
        .retryExceptions(FeignException.FeignServerException.class)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(100, 2))
        .failAfterMaxAttempts(true)
        .build();
    final RetryRegistry registry = RetryRegistry.of(config);
    TaggedRetryMetrics.ofRetryRegistry(registry)
        .bindTo(meterRegistry);
    return registry.retry("DEFAULT");
  }

  /**
   * The interface Binder.
   */
  @Module
  interface Binder {

    /**
     * Meter registry meter registry.
     *
     * @return the meter registry
     */
    @BindsOptionalOf
    MeterRegistry meterRegistry();

  }

}
