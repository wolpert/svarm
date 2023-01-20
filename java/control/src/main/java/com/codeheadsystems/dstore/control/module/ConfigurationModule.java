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

import com.codeheadsystems.dstore.control.ControlConfiguration;
import dagger.Module;
import dagger.Provides;
import io.dropwizard.setup.Environment;
import javax.inject.Singleton;

/**
 * Exposing the configuration.
 */
@Module
public class ConfigurationModule {

  private final ControlConfiguration configuration;
  private final Environment environment;


  /**
   * Constructor.
   *
   * @param configuration our configuration.
   * @param environment   our environment.
   */
  public ConfigurationModule(final ControlConfiguration configuration,
                             final Environment environment) {
    this.configuration = configuration;
    this.environment = environment;
  }

  /**
   * Accessor to the configuration.
   *
   * @return the configuration.
   */
  @Provides
  @Singleton
  public ControlConfiguration configuration() {
    return configuration;
  }

  /**
   * Accessor to environment.
   *
   * @return the environment.
   */
  @Provides
  @Singleton
  public Environment environment() {
    return environment;
  }

}
