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

import dagger.Module;
import dagger.Provides;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import javax.inject.Singleton;

/**
 * Utilities we need internally.
 */
@Module
public class UtilitiesModule {

  /**
   * Instantiates a new Utilities module.
   */
  public UtilitiesModule() {
  }

  /**
   * Random generator for our app.
   *
   * @return a secure random generator.
   */
  @Provides
  @Singleton
  public RandomGenerator randomGenerator() {
    return new SecureRandom();
  }

}
