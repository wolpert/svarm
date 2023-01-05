/*
 * Copyright (c) 2022-2023. Ned Wolpert
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

package com.codeheadsystems.dstore.node.component;

import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.dstore.common.module.JsonModule;
import com.codeheadsystems.dstore.node.module.ConfigurationModule;
import com.codeheadsystems.dstore.node.module.HealthCheckModule;
import com.codeheadsystems.dstore.node.module.ResourceModule;
import com.codeheadsystems.dstore.node.module.StartupModule;
import com.codeheadsystems.dstore.node.module.UtilitiesModule;
import com.codeheadsystems.dstore.node.resource.JerseyResource;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import dagger.Component;
import io.dropwizard.lifecycle.Managed;
import java.util.Set;
import javax.inject.Singleton;

/**
 * All the necessary goodness to create the app.
 */
@Singleton
@Component(modules = {
    ConfigurationModule.class,
    HealthCheckModule.class,
    JsonModule.class,
    MetricsModule.class,
    ResourceModule.class,
    StartupModule.class,
    UtilitiesModule.class
})
public interface DropWizardComponent {

  /**
   * Returns the resources for the application.
   *
   * @return resources.
   */
  Set<JerseyResource> resources();

  /**
   * Returns the health check for the application.
   *
   * @return the health checks.
   */
  Set<HealthCheck> healthChecks();

  /**
   * Returns the startup services that need to function before registering resources and health checks.
   *
   * @return the managed services.
   */
  Set<Managed> managed();
}
