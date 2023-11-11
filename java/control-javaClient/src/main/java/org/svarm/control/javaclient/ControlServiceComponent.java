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

package org.svarm.control.javaclient;

import dagger.Component;
import javax.inject.Singleton;
import org.svarm.common.javaclient.JavaClientModule;
import org.svarm.common.javaclient.StandaloneMeterRegistry;
import org.svarm.common.module.CommonModule;
import org.svarm.control.common.api.ControlNodeService;
import org.svarm.control.common.api.ControlTenantResourceService;
import org.svarm.control.javaclient.module.ControlServiceModule;

/**
 * Creates a NodeService java client.
 */
@Component(modules = {
    CommonModule.class,
    ControlServiceModule.class,
    JavaClientModule.class,
    StandaloneMeterRegistry.class
})
@Singleton
public interface ControlServiceComponent {

  /**
   * Usable to create a fresh java client.
   *
   * @param connectionUrl to connect to.
   * @return a node service impl.
   */
  static ControlServiceComponent controlNodeService(final String connectionUrl) {
    return DaggerControlServiceComponent.builder()
        .controlServiceModule(new ControlServiceModule(connectionUrl))
        .build();
  }

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  ControlNodeService controlNodeService();

  /**
   * The control tenant table service.
   *
   * @return the service.
   */
  ControlTenantResourceService controlTenantResourceService();


}
