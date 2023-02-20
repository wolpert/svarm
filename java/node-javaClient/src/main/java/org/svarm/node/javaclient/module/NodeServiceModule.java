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

package org.svarm.node.javaclient.module;

import dagger.Module;
import dagger.Provides;
import feign.Feign;
import javax.inject.Singleton;
import org.svarm.node.api.NodeTenantService;
import org.svarm.node.api.NodeTenantTableEntryService;
import org.svarm.node.api.NodeTenantTableService;

/**
 * Module for the node service.
 */
@Module
public class NodeServiceModule {

  private final String connectionUrl;

  /**
   * Constructor.
   *
   * @param connectionUrl used to connect to node.
   */
  public NodeServiceModule(final String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }

  /**
   * Get a usable node service.
   *
   * @param builder feign builder to use.
   * @return a node service.
   */
  @Provides
  @Singleton
  public NodeTenantService nodeTenantService(final Feign.Builder builder) {
    return builder.target(NodeTenantService.class, connectionUrl);
  }

  /**
   * Get a usable node service.
   *
   * @param builder feign builder to use.
   * @return a node service.
   */
  @Provides
  @Singleton
  public NodeTenantTableService nodeTenantTableService(final Feign.Builder builder) {
    return builder.target(NodeTenantTableService.class, connectionUrl);
  }

  /**
   * Get a usable node service.
   *
   * @param builder feign builder to use.
   * @return a node service.
   */
  @Provides
  @Singleton
  public NodeTenantTableEntryService nodeTenantTableEntryService(final Feign.Builder builder) {
    return builder.target(NodeTenantTableEntryService.class, connectionUrl);
  }
}
