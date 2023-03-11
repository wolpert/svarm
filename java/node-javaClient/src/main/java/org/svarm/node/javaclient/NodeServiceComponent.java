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

package org.svarm.node.javaclient;

import dagger.Component;
import javax.inject.Singleton;
import org.svarm.common.javaclient.JavaClientModule;
import org.svarm.common.javaclient.StandaloneMeterRegistry;
import org.svarm.common.module.CommonModule;
import org.svarm.node.api.NodeTenantService;
import org.svarm.node.api.NodeTenantTableEntryService;
import org.svarm.node.api.NodeTenantTableService;
import org.svarm.node.javaclient.module.NodeServiceModule;

/**
 * Creates a NodeService java client.
 */
@Component(modules = {
    JavaClientModule.class,
    CommonModule.class,
    NodeServiceModule.class,
    StandaloneMeterRegistry.class
})
@Singleton
public interface NodeServiceComponent {

  /**
   * Usable to create a fresh java client.
   *
   * @param connectionUrl to connect to.
   * @return a node service impl.
   */
  static NodeServiceComponent generate(final String connectionUrl) {
    return DaggerNodeServiceComponent.builder()
        .nodeServiceModule(new NodeServiceModule(connectionUrl))
        .build();
  }

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  NodeTenantService nodeTenantService();

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  NodeTenantTableService nodeTenantTableService();

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  NodeTenantTableEntryService nodeTenantTableEntryService();

}
