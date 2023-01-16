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

package com.codeheadsystems.dstore.node;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.dstore.node.component.DaggerDropWizardComponent;
import com.codeheadsystems.dstore.node.component.DropWizardComponent;
import com.codeheadsystems.dstore.node.module.ConfigurationModule;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import com.codeheadsystems.metrics.helper.DropwizardMetricsHelper;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is our application itself. Very little here is node specific.
 */
public class Node extends Application<NodeConfiguration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

  /**
   * Default constructor.
   */
  public Node() {
    LOGGER.info("Node()");
  }

  /**
   * Run the world.
   *
   * @param args from the command line.
   * @throws Exception if we could not start the server.
   */
  public static void main(String[] args) throws Exception {
    LOGGER.info("main({})", (Object) args);
    final Node server = new Node();
    server.run(args);
  }

  /**
   * Runs the application.
   *
   * @param configuration the parsed {@link NodeConfiguration} object
   * @param environment   the application's {@link Environment}
   * @throws Exception if everything dies.
   */
  @Override
  public void run(final NodeConfiguration configuration,
                  final Environment environment) throws Exception {
    LOGGER.info("run({},{})", configuration, environment);
    final MetricRegistry metricRegistry = environment.metrics();
    final MeterRegistry meterRegistry = new DropwizardMetricsHelper().instrument(metricRegistry);
    final DropWizardComponent component = DaggerDropWizardComponent.builder()
        .configurationModule(new ConfigurationModule(configuration, metricRegistry))
        .metricsModule(new MetricsModule(meterRegistry))
        .build();
    final JerseyEnvironment jerseyEnvironment = environment.jersey();
    for (Object resource : component.resources()) {
      LOGGER.info("Registering resource: {}", resource.getClass().getSimpleName());
      jerseyEnvironment.register(resource);
    }
    for (HealthCheck healthCheck : component.healthChecks()) {
      LOGGER.info("Registering healthCheck: {}", healthCheck.getClass().getSimpleName());
      environment.healthChecks().register(healthCheck.getClass().getSimpleName(), healthCheck);
    }
  }

}
