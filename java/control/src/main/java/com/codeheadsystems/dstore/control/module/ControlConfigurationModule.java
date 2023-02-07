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

package com.codeheadsystems.dstore.control.module;

import com.codeheadsystems.dstore.common.config.EtcdConfiguration;
import com.codeheadsystems.dstore.control.ControlConfiguration;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * For setting up the configuration for the service.
 */
@Module
public class ControlConfigurationModule {

  /**
   * Provider for the etcd configuration.
   *
   * @param configuration from us.
   * @return etcd config.
   */
  @Provides
  @Singleton
  public EtcdConfiguration configuration(final ControlConfiguration configuration) {
    return configuration.getEtcdConfiguration();
  }

}
