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

package org.svarm.server.module;

import static org.svarm.common.module.CommonModule.SERVER_OBJECT_MAPPER;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.metrics.MetricFactory;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.TagsGenerator;
import com.codeheadsystems.metrics.helper.TagsGeneratorRegistry;
import com.codeheadsystems.metrics.impl.MetricPublisher;
import com.codeheadsystems.metrics.impl.MicrometerMetricsPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import dagger.multibindings.Multibinds;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.inject.Named;
import javax.inject.Singleton;
import org.svarm.common.config.EtcdConfiguration;
import org.svarm.common.engine.TraceUuidEngine;
import org.svarm.common.module.CommonModule;
import org.svarm.server.ServerConfiguration;
import org.svarm.server.initializer.HealthCheckInitializer;
import org.svarm.server.initializer.Initializer;
import org.svarm.server.initializer.JerseyResourceInitializer;
import org.svarm.server.initializer.ManagedObjectInitializer;
import org.svarm.server.resource.JerseyResource;
import org.svarm.server.resource.MetricTagsResource;
import org.svarm.server.resource.NotFoundExceptionMapper;
import org.svarm.server.resource.TraceUuidResource;

/**
 * Contains expected modules for dropwizard.
 */
@Module(includes = {
    CommonModule.class,
    DropWizardModule.Binder.class
})
public class DropWizardModule {

  /**
   * Identifier for the application name.
   */
  public static final String DROPWIZARD_APPLICATION_NAME = "Dropwizard Application Name";
  /**
   * Identifier for the deployment stage.
   */
  public static final String DEPLOYMENT_STAGE = "Deployment stage";
  private final TraceUuidEngine engine;
  private final MetricRegistry metricRegistry;
  private final Environment environment;
  private final ServerConfiguration configuration;
  private final String applicationName;

  /**
   * Constructor.
   *
   * @param engine        to use.
   * @param environment   for the environment.
   * @param configuration the configuration.
   */
  public DropWizardModule(final TraceUuidEngine engine,
                          final Environment environment,
                          final ServerConfiguration configuration) {
    this.engine = engine;
    this.metricRegistry = environment.metrics();
    this.environment = environment;
    this.configuration = configuration;
    this.applicationName = environment.getName();
  }

  private static String getHost() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Overrides the factory object mapper with the server one.
   *
   * @param environment from dropwizard.
   * @return the object mapper.
   */
  @Provides
  @Singleton
  @Named(SERVER_OBJECT_MAPPER)
  public ObjectMapper serverObjectMapper(final Environment environment) {
    return environment.getObjectMapper();
  }

  /**
   * Accessor to application name.
   *
   * @return the the name.
   */
  @Provides
  @Singleton
  @Named(DROPWIZARD_APPLICATION_NAME)
  public String applicationName() {
    return applicationName;
  }


  /**
   * Accessor to application name.
   *
   * @return the the name.
   */
  @Provides
  @Singleton
  @Named(DEPLOYMENT_STAGE)
  public String deploymentStage() {
    return configuration.getStage();
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
  public ServerConfiguration serverConfiguration() {
    return configuration;
  }

  /**
   * Accessor to configuration.
   *
   * @param serverConfiguration the server configuration.
   * @return the configuration.
   */
  @Provides
  @Singleton
  public EtcdConfiguration etcdConfiguration(final ServerConfiguration serverConfiguration) {
    return configuration.getEtcdConfiguration();
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
   * @return registry. metric registry
   */
  @Provides
  @Singleton
  public MetricRegistry metricRegistry() {
    return metricRegistry;
  }

  /**
   * Metric publisher metric publisher.
   *
   * @param meterRegistry the metric registry
   * @return the metric publisher
   */
  @Provides
  @Singleton
  public MetricPublisher metricPublisher(final MeterRegistry meterRegistry) {
    return new MicrometerMetricsPublisher(meterRegistry);
  }

  /**
   * Meter registry meter registry.
   *
   * @param metricRegistry the metric registry
   * @return the meter registry
   */
  @Provides
  @Singleton
  public MeterRegistry meterRegistry(final MetricRegistry metricRegistry) {
    final DropwizardConfig config = new DropwizardConfig() {
      @Override
      public String prefix() {
        return "svarm";
      }

      @Override
      public String get(final String key) {
        return null;
      }
    };
    return new DropwizardMeterRegistry(config, metricRegistry, HierarchicalNameMapper.DEFAULT, io.micrometer.core.instrument.Clock.SYSTEM) {
      @Override
      protected Double nullGaugeValue() {
        return null;
      }
    };
  }

  /**
   * Tags generator registry tags generator registry.
   *
   * @return the tags generator registry
   */
  @Provides
  @Singleton
  public TagsGeneratorRegistry tagsGeneratorRegistry() {
    return new TagsGeneratorRegistry();
  }

  /**
   * Metric factory metric factory.
   *
   * @param metricPublisher       the metric publisher
   * @param defaultTags           the default tags
   * @param clock                 the clock
   * @param tagsGeneratorRegistry the tags generator registry
   * @return the metric factory
   */
  @Provides
  @Singleton
  public MetricFactory metricFactory(final MetricPublisher metricPublisher,
                                     final Tags defaultTags,
                                     final Clock clock,
                                     final TagsGeneratorRegistry tagsGeneratorRegistry) {
    return MetricFactory.builder()
        .withMetricPublisher(metricPublisher)
        .withTags(defaultTags)
        .withClock(clock)
        .withCloseAndOpenOnlyForInitial(false)
        .withDefaultTagsGeneratorForThrowable(new TagsGenerator<Throwable>() {
          @Override
          public Tags from(final Throwable throwable) {
            return Tags.of("exception", throwable.getClass().getSimpleName());
          }
        })
        .withTagsGeneratorRegistry(tagsGeneratorRegistry)
        .build();
  }

  /**
   * The instrumented meter registry.
   *
   * @param deployment      the deployment
   * @param applicationName the application name
   * @return registry. tags
   */
  @Provides
  @Singleton
  public Tags defaultTags(final @Named(DEPLOYMENT_STAGE) String deployment,
                          final @Named(DROPWIZARD_APPLICATION_NAME) String applicationName
  ) {
    return Tags.of(
        "host", getHost(),
        "application", applicationName,
        "stage", deployment);
  }

  /**
   * Returns the supplier for the default tags.
   *
   * @param defaultTags base set.
   * @return the supplier.
   */
  @Provides
  @Singleton
  public Supplier<Tags> defaultTagSupplier(final Tags defaultTags) {
    final Optional<Tags> optional = Optional.of(defaultTags);
    return optional::get;
  }

  /**
   * All of the stuff the server needs.
   */
  @Module
  public interface Binder {

    /**
     * Returns the resources for the application.
     *
     * @return resources. set
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
     * Initializer provider.
     *
     * @param initializer the specific initializer.
     * @return the generic initializer.
     */
    @Binds
    @IntoSet
    Initializer healthCheckInitializer(HealthCheckInitializer initializer);

    /**
     * Initializer provider.
     *
     * @param initializer the specific initializer.
     * @return the generic initializer.
     */
    @Binds
    @IntoSet
    Initializer managedObjectInitializer(ManagedObjectInitializer initializer);

    /**
     * Initializer provider.
     *
     * @param initializer the specific initializer.
     * @return the generic initializer.
     */
    @Binds
    @IntoSet
    Initializer jerseyResourceInitializer(JerseyResourceInitializer initializer);

    /**
     * TraceUuidResource resource.
     *
     * @param resource resource.
     * @return JerseyResource. jersey resource
     */
    @Binds
    @IntoSet
    JerseyResource traceUuid(TraceUuidResource resource);

    /**
     * Not found exception mapper.
     *
     * @param resource resource.
     * @return JerseyResource. jersey resource
     */
    @Binds
    @IntoSet
    JerseyResource notFoundExceptionMapper(NotFoundExceptionMapper resource);

    /**
     * Metrics tag resource.
     *
     * @param resource resource.
     * @return JerseyResource. jersey resource
     */
    @Binds
    @IntoSet
    JerseyResource metricTagsResource(MetricTagsResource resource);

    /**
     * Metrics metrics.
     *
     * @param metricFactory metric factory
     * @return Metrics. metrics
     */
    @Binds
    Metrics metrics(MetricFactory metricFactory);

  }
}
