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

import com.codeheadsystems.dstore.common.config.EtcdConfiguration;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.factory.NodeConfigurationFactory;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * The configuration for the node.
 */
@Module
public class NodeConfigurationModule {

  /**
   * Provider for the etcd configuration.
   *
   * @param configuration from us.
   * @return the etcd configuration.
   */
  @Provides
  @Singleton
  public EtcdConfiguration etcdConfiguration(final NodeConfiguration configuration) {
    return configuration.getEtcdConfiguration();
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
