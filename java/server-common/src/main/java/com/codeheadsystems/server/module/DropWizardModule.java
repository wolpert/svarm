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

import com.codahale.metrics.health.HealthCheck;
import com.codeheadsystems.dstore.common.module.JsonModule;
import com.codeheadsystems.metrics.dagger.MetricsModule;
import com.codeheadsystems.server.resource.JerseyResource;
import dagger.Module;
import dagger.multibindings.Multibinds;
import io.dropwizard.lifecycle.Managed;
import java.util.Set;

/**
 * Contains expected modules for dropwizard.
 */
@Module(includes = {
    JsonModule.class,
    MetricRegistryModule.class
})
public interface DropWizardModule {

  // --- multi-binds so we don't error if nothing is declared.


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

}