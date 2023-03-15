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

package org.svarm.common.config.engine;

import dagger.Component;
import io.etcd.jetcd.test.EtcdClusterExtension;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Singleton;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.svarm.common.config.EtcdConfiguration;
import org.svarm.common.config.ImmutableEtcdConfiguration;
import org.svarm.common.config.api.ImmutableMetaData;
import org.svarm.common.config.api.ImmutableNodeRange;
import org.svarm.common.config.api.ImmutableNodeTenantResource;
import org.svarm.common.config.api.ImmutableNodeTenantResourceRange;
import org.svarm.common.config.api.ImmutableTenantResource;
import org.svarm.common.config.api.ImmutableTenantResourceRange;
import org.svarm.common.config.api.NodeRange;
import org.svarm.common.config.api.NodeTenantResourceRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.api.TenantResourceRange;
import org.svarm.common.config.module.EtcdConfigurationModule;
import org.svarm.common.config.module.EtcdModule;
import org.svarm.common.module.CommonModule;

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
        .metaData(ImmutableMetaData.builder().hash(random.nextInt()).build())
        .build();
  }

  private static TenantResourceRange randomTenantResourceRange(final int count) {
    final Map<Integer, NodeRange> hashToNodeRange = IntStream.range(0, count)
        .mapToObj(i -> Map.entry(
            i * 23,
            ImmutableNodeRange.builder()
                    .hash(i)
                    .uri(uuid())
                    .uuid(uuid())
                    .build()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return ImmutableTenantResourceRange.builder()
        .resource(uuid())
        .tenant(uuid())
        .hashToNodeRange(hashToNodeRange)
        .build();
  }

  @BeforeEach
  void setupClient() {
    final List<String> endpoints = cluster.clientEndpoints().stream().map(Objects::toString).toList();
    final EtcdConfiguration configuration = ImmutableEtcdConfiguration.builder().endpoints(endpoints).build();
    final EtcdConfigurationModule etcdModule = new EtcdConfigurationModule(configuration);
    final EngineComponent component = DaggerNodeConfigurationEngineTest_EngineComponent.builder().etcdConfigurationModule(etcdModule).build();
    engine = component.engine();
  }

  @Test
  void testTenantResourceRange() {
    final TenantResourceRange trr = randomTenantResourceRange(3);
    engine.write(trr);
    final TenantResource tenantResource = ImmutableTenantResource.builder()
        .tenant(trr.tenant()).resource(trr.resource()).build();
    Assertions.assertThat(engine.readTenantResourceRange(tenantResource))
        .isPresent()
        .contains(trr);

    engine.delete(trr);
    Assertions.assertThat(engine.readTenantResourceRange(tenantResource))
        .isEmpty();
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

    engine.delete(resource1);
    final List<NodeTenantResourceRange> result2 = engine.readNodeResources(uuid);
    Assertions.assertThat(result2)
        .isNotNull()
        .hasSize(2)
        .contains(resource2, resource3);
  }

  @Component(modules = {EtcdModule.class, EtcdConfigurationModule.class, CommonModule.class})
  @Singleton
  public interface EngineComponent {
    NodeConfigurationEngine engine();
  }

}