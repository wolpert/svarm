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

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.svarm.node.NodeConfiguration;
import org.svarm.node.manager.ControlPlaneManager;
import org.svarm.node.manager.FakeControlPlaneManager;
import org.svarm.node.manager.RealControlPlaneManager;

/**
 * Connection information for the control plane.
 */
@Module(includes = {})
public class ControlPlaneModule {

  /**
   * Instantiates a new Control plane module.
   */
  public ControlPlaneModule() {
  }

  /**
   * This will be removed once we have the real control plane.
   *
   * @param fakeControlPlaneManager fake.
   * @param realControlPlaneManager real.
   * @param nodeConfiguration       for returning the one we want.
   * @return fake. control plane manager
   */
  @Provides
  @Singleton
  public ControlPlaneManager controlPlaneManager(final Lazy<FakeControlPlaneManager> fakeControlPlaneManager,
                                                 final Lazy<RealControlPlaneManager> realControlPlaneManager,
                                                 final NodeConfiguration nodeConfiguration) {
    if (nodeConfiguration.isDisableControlPlane()) {
      return fakeControlPlaneManager.get();
    } else {
      return realControlPlaneManager.get();
    }
  }

}
