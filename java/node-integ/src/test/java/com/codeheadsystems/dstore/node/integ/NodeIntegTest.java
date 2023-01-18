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

package com.codeheadsystems.dstore.node.integ;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.dstore.common.factory.ObjectMapperFactory;
import com.codeheadsystems.dstore.node.Node;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.api.NodeService;
import com.codeheadsystems.dstore.node.javaclient.NodeServiceComponent;
import com.codeheadsystems.test.utils.DeletingFileVisitor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
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
import org.junit.jupiter.api.Test;

public class NodeIntegTest {

  private static DropwizardTestSupport<NodeConfiguration> SUPPORT;
  private static Path BASE_DIRECTORY_PATH;
  private static NodeService NODE_SERVICE;
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
    NODE_SERVICE = NodeServiceComponent.generate(CONNECTION_URL);
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
    System.out.println(key + " Setting nodeService");
    final NodeService service = NodeServiceComponent.generate(CONNECTION_URL);
    System.out.println(key + " creating entry");
    service.createTenantTableEntry(tenant, table, key, value);
    System.out.println(key + " reading entry");
    service.readTenantTableEntry(tenant, table, key);
    System.out.println(key + " done");
  }

  @Test
  void oneTenantMultipleTables() {
    final String tenant = "oneTenantMultipleTables";
    final String table1 = UUID.randomUUID().toString();
    final String table2 = UUID.randomUUID().toString();

    final Map<String, JsonNode> t1Data = randomData(5);
    final Map<String, JsonNode> t2Data = randomData(6);
    NODE_SERVICE.createTenant(tenant);
    NODE_SERVICE.createTenantTable(tenant, table1, "ignored");
    NODE_SERVICE.createTenantTable(tenant, table2, "ignored");
    t1Data.forEach((k, v) -> NODE_SERVICE.createTenantTableEntry(tenant, table1, k, v));
    t2Data.forEach((k, v) -> NODE_SERVICE.createTenantTableEntry(tenant, table2, k, v));

    // Verify
    t1Data.forEach((k, v) -> assertThat(NODE_SERVICE.readTenantTableEntry(tenant, table1, k)).isEqualTo(v));
    t2Data.forEach((k, v) -> assertThat(NODE_SERVICE.readTenantTableEntry(tenant, table2, k)).isEqualTo(v));

    NODE_SERVICE.deleteTenantTable(tenant, table1);
    NODE_SERVICE.deleteTenantTable(tenant, table2);
    NODE_SERVICE.deleteTenant(tenant);
  }

  // Testing this in the dropwizard test isn't doing what I want it to do. We should use a real load test to see
  // what we can do.
  void oneTenantLotsOfConcurrentThreadsWriting() throws InterruptedException {
    final String tenant = "oneTenantLotsOfConcurrentThreadsWriting";
    final String table = UUID.randomUUID().toString();
    final int threads = 30;

    final Map<String, JsonNode> data = randomData(threads);
    NODE_SERVICE.createTenant(tenant);
    NODE_SERVICE.createTenantTable(tenant, table, "ignored");

    final ForkJoinPool pool = new ForkJoinPool(threads);
    final List<ForkJoinTask<?>> tasks = data.entrySet().stream()
        .map((e) -> pool.submit(() -> oneTest(tenant, table, e)))
        .collect(Collectors.toList());
    System.out.println("Sent, now waiting to join");
    tasks.forEach(ForkJoinTask::join);
    data.forEach((k, v) -> assertThat(NODE_SERVICE.readTenantTableEntry(tenant, table, k)).isEqualTo(v));

    NODE_SERVICE.deleteTenantTable(tenant, table);
    NODE_SERVICE.deleteTenant(tenant);
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

    assertThat(NODE_SERVICE.listTenants()).doesNotContain(tenant);
    assertThat(NODE_SERVICE.createTenant(tenant)).hasFieldOrPropertyWithValue("id", tenant);
    assertThat(NODE_SERVICE.listTenants()).contains(tenant);

    assertThat(NODE_SERVICE.listTenantTables(tenant)).isEmpty();
    assertThat(NODE_SERVICE.createTenantTable(tenant, table, "ignored")).hasFieldOrPropertyWithValue("id", table);
    assertThat(NODE_SERVICE.listTenantTables(tenant)).containsExactly(table);

    final JsonNode j1 = OBJECT_MAPPER.createObjectNode().put("One", "a thing").put("two", 2);
    final JsonNode j2 = OBJECT_MAPPER.createObjectNode().put("Free", "a different thing").put("four", 4);

    final String j1Entry = "something";
    final String j2Entry = "different";

    NODE_SERVICE.createTenantTableEntry(tenant, table, j1Entry, j1);
    NODE_SERVICE.createTenantTableEntry(tenant, table, j2Entry, j2);

    assertThat(NODE_SERVICE.readTenantTableEntry(tenant, table, j1Entry)).isEqualTo(j1);
    assertThat(NODE_SERVICE.readTenantTableEntry(tenant, table, j2Entry)).isEqualTo(j2);

    NODE_SERVICE.deleteTenantTableEntry(tenant, table, j1Entry);
    assertThatExceptionOfType(FeignException.NotFound.class)
        .isThrownBy(() -> NODE_SERVICE.readTenantTableEntry(tenant, table, j1Entry));

    NODE_SERVICE.deleteTenantTable(tenant, table);
    assertThat(NODE_SERVICE.listTenantTables(tenant)).isEmpty();
    NODE_SERVICE.deleteTenant(tenant);
    assertThat(NODE_SERVICE.listTenants()).doesNotContain(tenant);
  }
}
