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

package org.svarm.node.module;

import static org.svarm.common.config.module.EtcdModule.WATCH_ENGINE_EXECUTOR;
import static org.svarm.control.javaclient.module.ControlServiceModule.CONTROL_SERVICE_CONNECTION_URL;

import dagger.Module;
import dagger.Provides;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Named;
import javax.inject.Singleton;
import org.svarm.node.NodeConfiguration;
import org.svarm.node.factory.NodeConfigurationFactory;
import org.svarm.node.model.NodeInternalConfiguration;
import org.svarm.server.ServerConfiguration;

/**
 * The configuration for the node.
 */
@Module
public class NodeConfigurationModule {

  /**
   * Converts the configuration to a node configuration.
   *
   * @param configuration from dropwizard.
   * @return our type.
   */
  @Provides
  @Singleton
  public NodeConfiguration configuration(final ServerConfiguration configuration) {
    return (NodeConfiguration) configuration;
  }

  /**
   * Provides the internal node configuration for us.
   *
   * @param factory       a factory to build or read.
   * @param configuration startup configuration.
   * @return internal configuration.
   */
  @Provides
  @Singleton
  public NodeInternalConfiguration nodeInternalConfiguration(final NodeConfigurationFactory factory,
                                                             final NodeConfiguration configuration) {
    return factory.readOrGenerate(configuration.getDatabaseDirectory());
  }

  /**
   * Provides the control plane url.
   *
   * @param nodeConfiguration the configuration.
   * @return the control plane.
   */
  @Provides
  @Singleton
  @Named(CONTROL_SERVICE_CONNECTION_URL)
  public String controlServiceConnectionUrl(final NodeConfiguration nodeConfiguration) {
    return nodeConfiguration.getControlPlaneUrl();
  }

  /**
   * Gets an executor service for the watch engine.
   *
   * @param nodeConfiguration to get the count from.
   * @return the service.
   */
  @Provides
  @Singleton
  @Named(WATCH_ENGINE_EXECUTOR)
  public ExecutorService executorService(final NodeConfiguration nodeConfiguration) {
    return Executors.newFixedThreadPool(nodeConfiguration.getWatchEngineThreads());
  }


}
