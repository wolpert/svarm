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

package com.codeheadsystems.dstore.endtoend.environment;

import com.codeheadsystems.dstore.endtoend.EnvironmentConfiguration;
import io.etcd.jetcd.test.EtcdClusterExtension;
import java.util.stream.Collectors;

public class EtcdServiceManager implements ServiceManager {
  private EtcdClusterExtension clusterExtension;

  @Override
  public void startup(EnvironmentConfiguration configuration) {
    clusterExtension = EtcdClusterExtension.builder().withNodes(1).build();
    clusterExtension.cluster().start();
    configuration.setEndpoints(clusterExtension
        .clientEndpoints()
        .stream()
        .map(u -> u.toString())
        .collect(Collectors.toList()));
  }

  @Override
  public void shutdown(EnvironmentConfiguration configuration) {
    clusterExtension.cluster().stop();
    clusterExtension = null;
  }
}
