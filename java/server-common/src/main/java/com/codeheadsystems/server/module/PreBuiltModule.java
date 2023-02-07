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

package com.codeheadsystems.server.module;

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.dstore.common.engine.TraceUuidEngine;
import com.codeheadsystems.metrics.helper.DropwizardMetricsHelper;
import dagger.Module;
import dagger.Provides;
import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Singleton;

/**
 * Use this to provide internal components.
 */
@Module
public class PreBuiltModule {

  private final TraceUuidEngine engine;
  private final MetricRegistry metricRegistry;
  private final MeterRegistry meterRegistry;

  /**
   * Constructor.
   *
   * @param engine to use.
   * @param metricRegistry for metrics.
   */
  public PreBuiltModule(final TraceUuidEngine engine,
                        final MetricRegistry metricRegistry) {
    this.engine = engine;
    this.metricRegistry = metricRegistry;
    this.meterRegistry = new DropwizardMetricsHelper().instrument(metricRegistry);
  }

  /**
   * Provider.
   *
   * @return the engine.
   */
  @Provides
  @Singleton
  public TraceUuidEngine engine() {
    return engine;
  }

  /**
   * Add the metric registry to dagger context.
   *
   * @return registry.
   */
  @Provides
  @Singleton
  public MetricRegistry metricRegistry() {
    return metricRegistry;
  }

  /**
   * The instrumented meter registry.
   *
   * @return registry.
   */
  @Provides
  @Singleton
  @javax.inject.Named("Meter Registry")
  public MeterRegistry meterRegistry() {
    return meterRegistry;
  }
}
