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

import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.factory.NodeConfigurationFactory;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Provides access to the configuration itself.
 */
@Module
public class ConfigurationModule {

  private final NodeConfiguration nodeConfiguration;

  /**
   * Default constructor.
   *
   * @param configuration node configuration from dropwizard.
   */
  public ConfigurationModule(final NodeConfiguration configuration) {
    this.nodeConfiguration = configuration;
  }

  /**
   * Provide the configuration for others.
   *
   * @return node configuration.
   */
  @Provides
  @Singleton
  public NodeConfiguration nodeConfiguration() {
    return nodeConfiguration;
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

}
