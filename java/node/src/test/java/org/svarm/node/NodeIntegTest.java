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

package org.svarm.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.test.utils.DeletingFileVisitor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.etcd.jetcd.test.EtcdClusterExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.svarm.common.factory.ObjectMapperFactory;
import org.svarm.node.api.NodeTenantService;
import org.svarm.node.api.NodeTenantTableEntryService;
import org.svarm.node.api.NodeTenantTableService;
import org.svarm.node.javaclient.NodeServiceComponent;

@Tag("integ")
public class NodeIntegTest {

  @RegisterExtension
  public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
      .withNodes(1)
      .build();

  private static DropwizardTestSupport<NodeConfiguration> SUPPORT;
  private static Path BASE_DIRECTORY_PATH;
  private static NodeTenantService NODE_TENANT;
  private static NodeTenantTableService NODE_TABLE;
  private static NodeTenantTableEntryService NODE_ENTRY;
  private static ObjectMapper OBJECT_MAPPER;
  private static Random RANDOM;
  private static String CONNECTION_URL;

  @BeforeAll
  static void setup() throws Exception {
    BASE_DIRECTORY_PATH = Files.createTempDirectory("NodeIntegTest-");
    System.out.println("Made directory: " + BASE_DIRECTORY_PATH.toString());
    SUPPORT = new DropwizardTestSupport<>(
        Node.class,
        ResourceHelpers.resourceFilePath("config.yaml"),
        ConfigOverride.config("databaseDirectory", BASE_DIRECTORY_PATH.toString())
    );
    SUPPORT.before();
    CONNECTION_URL = "http://localhost:" + SUPPORT.getLocalPort() + "/";
    final NodeServiceComponent component = NodeServiceComponent.generate(CONNECTION_URL);
    NODE_TENANT = component.nodeTenantService();
    NODE_TABLE = component.nodeTenantTableService();
    NODE_ENTRY = component.nodeTenantTableEntryService();
    OBJECT_MAPPER = new ObjectMapperFactory().generate();
    RANDOM = new Random();
  }

  @AfterAll
  static void tearDown() {
    SUPPORT.after();
    try {
      Files.walkFileTree(BASE_DIRECTORY_PATH, new DeletingFileVisitor());
    } catch (IOException re) {
      System.out.println("Log: Unable to delete " + BASE_DIRECTORY_PATH + ":" + re.getClass());
    }
    BASE_DIRECTORY_PATH = null;
  }

  private static void oneTest(final String tenant, final String table, final Map.Entry<String, JsonNode> e) {
    final String key = e.getKey();
    final JsonNode value = e.getValue();
    System.out.println(key + " creating entry");
    NODE_ENTRY.createTenantTableEntry(tenant, table, key, value);
    System.out.println(key + " reading entry");
    NODE_ENTRY.readTenantTableEntry(tenant, table, key);
    System.out.println(key + " done");
  }

  @Test
  void oneTenantMultipleTables() {
    final String tenant = "oneTenantMultipleTables";
    final String table1 = UUID.randomUUID().toString();
    final String table2 = UUID.randomUUID().toString();

    final Map<String, JsonNode> t1Data = randomData(5);
    final Map<String, JsonNode> t2Data = randomData(6);
    NODE_TENANT.createTenant(tenant);
    NODE_TABLE.createTenantTable(tenant, table1);
    NODE_TABLE.createTenantTable(tenant, table2);
    t1Data.forEach((k, v) -> NODE_ENTRY.createTenantTableEntry(tenant, table1, k, v));
    t2Data.forEach((k, v) -> NODE_ENTRY.createTenantTableEntry(tenant, table2, k, v));

    // Verify
    t1Data.forEach((k, v) -> assertThat(NODE_ENTRY.readTenantTableEntry(tenant, table1, k)).contains(v));
    t2Data.forEach((k, v) -> assertThat(NODE_ENTRY.readTenantTableEntry(tenant, table2, k)).contains(v));

    NODE_TABLE.deleteTenantTable(tenant, table1);
    NODE_TABLE.deleteTenantTable(tenant, table2);
    NODE_TENANT.deleteTenant(tenant);
  }

  // Testing this in the dropwizard test isn't doing what I want it to do. We should use a real load test to see
  // what we can do.
  void oneTenantLotsOfConcurrentThreadsWriting() throws InterruptedException {
    final String tenant = "oneTenantLotsOfConcurrentThreadsWriting";
    final String table = UUID.randomUUID().toString();
    final int threads = 30;

    final Map<String, JsonNode> data = randomData(threads);
    NODE_TENANT.createTenant(tenant);
    NODE_TABLE.createTenantTable(tenant, table);

    final ForkJoinPool pool = new ForkJoinPool(threads);
    final List<ForkJoinTask<?>> tasks = data.entrySet().stream()
        .map((e) -> pool.submit(() -> oneTest(tenant, table, e)))
        .collect(Collectors.toList());
    System.out.println("Sent, now waiting to join");
    tasks.forEach(ForkJoinTask::join);
    data.forEach((k, v) -> assertThat(NODE_ENTRY.readTenantTableEntry(tenant, table, k)).isEqualTo(v));

    NODE_TABLE.deleteTenantTable(tenant, table);
    NODE_TENANT.deleteTenant(tenant);
  }

  private Map<String, JsonNode> randomData(final int elements) {
    final ImmutableMap.Builder<String, JsonNode> builder = ImmutableMap.builder();
    IntStream.range(0, elements).forEach(i -> builder.put(
        "data-" + i + "-" + UUID.randomUUID(),
        OBJECT_MAPPER.createObjectNode()
            .put(UUID.randomUUID().toString(), UUID.randomUUID().toString())
            .put(UUID.randomUUID().toString(), UUID.randomUUID().toString())
            .put(UUID.randomUUID().toString(), RANDOM.nextInt())
    ));
    return builder.build();
  }

  @Test
  void fullRoundTrip() {
    final String tenant = "fullRoundTrip";
    final String table = "tableName";

    assertThat(NODE_TENANT.listTenants()).doesNotContain(tenant);
    assertThat(NODE_TENANT.createTenant(tenant)).hasFieldOrPropertyWithValue("id", tenant);
    assertThat(NODE_TENANT.listTenants()).contains(tenant);

    assertThat(NODE_TABLE.listTenantTables(tenant)).isEmpty();
    assertThat(NODE_TABLE.createTenantTable(tenant, table)).hasFieldOrPropertyWithValue("id", table);
    assertThat(NODE_TABLE.listTenantTables(tenant)).containsExactly(table);

    final JsonNode j1 = OBJECT_MAPPER.createObjectNode().put("One", "a thing").put("two", 2);
    final JsonNode j2 = OBJECT_MAPPER.createObjectNode().put("Free", "a different thing").put("four", 4);

    final String j1Entry = "something";
    final String j2Entry = "different";

    NODE_ENTRY.createTenantTableEntry(tenant, table, j1Entry, j1);
    NODE_ENTRY.createTenantTableEntry(tenant, table, j2Entry, j2);

    assertThat(NODE_ENTRY.readTenantTableEntry(tenant, table, j1Entry)).contains(j1);
    assertThat(NODE_ENTRY.readTenantTableEntry(tenant, table, j2Entry)).contains(j2);

    NODE_ENTRY.deleteTenantTableEntry(tenant, table, j1Entry);
    assertThatExceptionOfType(FeignException.NotFound.class)
        .isThrownBy(() -> NODE_ENTRY.readTenantTableEntry(tenant, table, j1Entry));

    NODE_TABLE.deleteTenantTable(tenant, table);
    assertThat(NODE_TABLE.listTenantTables(tenant)).isEmpty();
    NODE_TENANT.deleteTenant(tenant);
    assertThat(NODE_TENANT.listTenants()).doesNotContain(tenant);
  }
}
