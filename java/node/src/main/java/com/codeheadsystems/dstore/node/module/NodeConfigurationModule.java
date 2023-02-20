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

package com.codeheadsystems.dstore.node.module;

import static com.codeheadsystems.dstore.control.javaclient.module.ControlServiceModule.CONTROL_SERVICE_CONNECTION_URL;

import com.codeheadsystems.dstore.common.config.EtcdConfiguration;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.factory.NodeConfigurationFactory;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.codeheadsystems.server.ServerConfiguration;
import dagger.Module;
import dagger.Provides;
import io.dropwizard.Configuration;
import javax.inject.Named;
import javax.inject.Singleton;

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


}
