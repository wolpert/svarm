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

package com.codeheadsystems.server;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.dstore.common.engine.TraceUuidEngine;
import com.codeheadsystems.server.component.DropWizardComponent;
import com.codeheadsystems.server.module.DropWizardModule;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is our application itself. Very little here is node specific.
 */
public abstract class Server<T extends Configuration> extends Application<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

  /**
   * Default constructor.
   */
  public Server() {
    LOGGER.info("Server()");
  }

  /**
   * Implement this method to return the dropwizard component we will use.
   *
   * @param module for Server created stuff.
   * @return dropwizard component.
   */
  protected abstract DropWizardComponent dropWizardComponent(final DropWizardModule module);

  /**
   * Runs the application.
   *
   * @param configuration the parsed object
   * @param environment   the application's {@link Environment}
   * @throws Exception if everything dies.
   */
  @Override
  public void run(final T configuration,
                  final Environment environment) throws Exception {
    LOGGER.info("run({},{})", configuration, environment);
    LOGGER.info("\n---\n--- Server Setup Starting ---\n---");
    final TraceUuidEngine engine = new TraceUuidEngine();
    engine.set(getName() + ":init:" + UUID.randomUUID());
    final MetricRegistry metricRegistry = environment.metrics();
    final DropWizardModule module = new DropWizardModule(
        engine, metricRegistry, environment, configuration, getClass().getCanonicalName());
    final DropWizardComponent component = dropWizardComponent(module);
    final JerseyEnvironment jerseyEnvironment = environment.jersey();
    LOGGER.info("\n---\n--- Registering Managed Objects ---\n---");
    for (Managed managed : component.managedObjects()) {
      LOGGER.info("Registering managed object: {}", managed.getClass().getSimpleName());
      environment.lifecycle().manage(managed);
    }
    LOGGER.info("\n---\n--- Registering Resources ---\n---");
    for (Object resource : component.resources()) {
      LOGGER.info("Registering resource: {}", resource.getClass().getSimpleName());
      jerseyEnvironment.register(resource);
    }
    LOGGER.info("\n---\n--- Registering Health Checks ---\n---");
    for (HealthCheck healthCheck : component.healthChecks()) {
      LOGGER.info("Registering healthCheck: {}", healthCheck.getClass().getSimpleName());
      environment.healthChecks().register(healthCheck.getClass().getSimpleName(), healthCheck);
    }
    engine.clear();
    LOGGER.info("\n---\n--- Server Setup Complete ---\n---");
  }

}
