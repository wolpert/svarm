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
import io.etcd.jetcd.Client;
import io.etcd.jetcd.launcher.EtcdCluster;
import io.etcd.jetcd.test.EtcdClusterExtension;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdServiceManager implements ServiceManager {
  private EtcdClusterExtension clusterExtension;

  @Override
  public void startup(EnvironmentConfiguration configuration) {
    clusterExtension = EtcdClusterExtension.builder().withNodes(1).build();
    final EtcdCluster cluster = clusterExtension.cluster();
    cluster.start();
    final List<URI> endpoints = cluster.clientEndpoints();
    configuration.setEndpoints(endpoints.stream()
        .map(URI::toString)
        .collect(Collectors.toList()));
    configuration.setEtcdClient(Client.builder().endpoints(endpoints).build());
  }

  @Override
  public void shutdown(EnvironmentConfiguration configuration) {
    clusterExtension.cluster().stop();
    clusterExtension = null;
  }
}
