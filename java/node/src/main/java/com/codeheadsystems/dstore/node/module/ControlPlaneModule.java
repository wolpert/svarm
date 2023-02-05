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

import com.codeheadsystems.dstore.node.manager.ControlPlaneManager;
import com.codeheadsystems.dstore.node.manager.FakeControlPlaneManager;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Connection information for the control plane.
 */
@Module
public class ControlPlaneModule {

  /**
   * This will be removed once we have the real control plane.
   *
   * @param fakeControlPlaneManager fake.
   * @return fake.
   */
  @Provides
  @Singleton
  public ControlPlaneManager controlPlaneManager(final FakeControlPlaneManager fakeControlPlaneManager) {
    return fakeControlPlaneManager;
  }

}
