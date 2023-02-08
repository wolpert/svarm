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
import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.dstore.common.engine.TraceUuidEngine;
import com.codeheadsystems.dstore.common.module.JsonModule;
import com.codeheadsystems.metrics.helper.DropwizardMetricsHelper;
import com.codeheadsystems.server.resource.JerseyResource;
import com.codeheadsystems.server.resource.NotFoundExceptionMapper;
import com.codeheadsystems.server.resource.TraceUuidResource;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import dagger.multibindings.Multibinds;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Set;
import javax.inject.Singleton;

/**
 * Contains expected modules for dropwizard.
 */
@Module(includes = {
    JsonModule.class,
    DropWizardModule.Binder.class
})
public class DropWizardModule {

  private final TraceUuidEngine engine;
  private final MetricRegistry metricRegistry;
  private final MeterRegistry meterRegistry;
  private final Environment environment;
  private final Configuration configuration;

  /**
   * Constructor.
   *
   * @param engine         to use.
   * @param metricRegistry for metrics.
   * @param environment    for the environment.
   * @param configuration  the configuration.
   */
  public DropWizardModule(final TraceUuidEngine engine,
                          final MetricRegistry metricRegistry,
                          final Environment environment,
                          final Configuration configuration) {
    this.engine = engine;
    this.metricRegistry = metricRegistry;
    this.meterRegistry = new DropwizardMetricsHelper().instrument(metricRegistry);
    this.environment = environment;
    this.configuration = configuration;
  }

  /**
   * Accessor to environment.
   *
   * @return the environment.
   */
  @Provides
  @Singleton
  public Environment environment() {
    return environment;
  }

  /**
   * Accessor to configuration.
   *
   * @return the configuration.
   */
  @Provides
  @Singleton
  public Configuration configuration() {
    return configuration;
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

  /**
   * All of the stuff the server needs.
   */
  @Module
  public interface Binder {

    /**
     * Returns the resources for the application.
     *
     * @return resources.
     */
    @Multibinds
    Set<JerseyResource> resources();

    /**
     * Returns the health check for the application.
     *
     * @return the health checks.
     */
    @Multibinds
    Set<HealthCheck> healthChecks();

    /**
     * Objects that need their lifecycle managed.
     *
     * @return set of objects.
     */
    @Multibinds
    Set<Managed> managedObjects();

    /**
     * TraceUuidResource resource.
     *
     * @param resource resource.
     * @return JerseyResource.
     */
    @Binds
    @IntoSet
    JerseyResource traceUuid(TraceUuidResource resource);

    /**
     * Not found exception mapper.
     *
     * @param resource resource.
     * @return JerseyResource.
     */
    @Binds
    @IntoSet
    JerseyResource notFoundExceptionMapper(NotFoundExceptionMapper resource);

  }
}
