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

package com.codeheadsystems.dstore.common.config.engine;

import com.codeheadsystems.dstore.common.config.EtcdConfiguration;
import com.codeheadsystems.dstore.common.config.ImmutableEtcdConfiguration;
import com.codeheadsystems.dstore.common.config.api.ImmutableNodeTenantResource;
import com.codeheadsystems.dstore.common.config.api.ImmutableNodeTenantResourceRange;
import com.codeheadsystems.dstore.common.config.api.ImmutableRange;
import com.codeheadsystems.dstore.common.config.api.ImmutableTenantResource;
import com.codeheadsystems.dstore.common.config.api.NodeTenantResourceRange;
import com.codeheadsystems.dstore.common.config.module.EtcdModule;
import com.codeheadsystems.dstore.common.module.JsonModule;
import dagger.Component;
import io.etcd.jetcd.test.EtcdClusterExtension;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import javax.inject.Singleton;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("integ")
class NodeConfigurationEngineTest {

  @RegisterExtension
  public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
      .withNodes(1)
      .build();
  private static final Random random = new Random();
  protected NodeConfigurationEngine engine;

  private static String uuid() {
    return UUID.randomUUID().toString();
  }

  private static NodeTenantResourceRange randomeNodeTenantResourceRange(final String node) {
    return ImmutableNodeTenantResourceRange.builder()
        .nodeTenantResource(ImmutableNodeTenantResource.builder().uuid(node)
            .tenantResource(ImmutableTenantResource.builder().tenant(uuid()).resource(uuid()).build()).build())
        .range(ImmutableRange.builder().highHash(random.nextInt()).lowHash(random.nextInt()).build())
        .build();
  }

  @BeforeEach
  void setupClient() {
    final List<String> endpoints = cluster.clientEndpoints().stream().map(Objects::toString).toList();
    final EtcdConfiguration configuration = ImmutableEtcdConfiguration.builder().endpoints(endpoints).build();
    final EtcdModule etcdModule = new EtcdModule(configuration);
    final EngineComponent component = DaggerNodeConfigurationEngineTest_EngineComponent.builder().etcdModule(etcdModule).build();
    engine = component.engine();
  }

  @Test
  void testRoundTrip() {
    final String uuid = uuid();
    final NodeTenantResourceRange resource1 = randomeNodeTenantResourceRange(uuid);
    final NodeTenantResourceRange resource2 = randomeNodeTenantResourceRange(uuid);
    final NodeTenantResourceRange resource3 = randomeNodeTenantResourceRange(uuid);

    engine.write(resource1);
    engine.write(resource2);
    engine.write(resource3);

    final List<NodeTenantResourceRange> result = engine.readNodeResources(uuid);
    Assertions.assertThat(result)
        .isNotNull()
        .hasSize(3)
        .contains(resource1, resource2, resource3);
  }

  @Component(modules = {EtcdModule.class, JsonModule.class})
  @Singleton
  public interface EngineComponent {
    NodeConfigurationEngine engine();
  }

}