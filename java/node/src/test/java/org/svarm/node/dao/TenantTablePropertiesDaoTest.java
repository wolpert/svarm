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

package org.svarm.node.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.node.BaseSQLTest;
import org.svarm.node.model.ImmutableTenantTableIdentifier;
import org.svarm.node.model.TenantTableIdentifier;

class TenantTablePropertiesDaoTest extends BaseSQLTest {

  private TenantTablePropertiesDao dao;

  private static TenantTableIdentifier randomIdentifier() {
    return ImmutableTenantTableIdentifier.builder()
        .tenantId(UUID.randomUUID().toString())
        .tableName(UUID.randomUUID().toString())
        .build();
  }

  private static Map<String, String> randomProperties(final int count) {
    final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    IntStream.range(0, count).forEach(i -> builder.put(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    return builder.build();
  }

  private static Map<String, String> limit(final Map<String, String> props, final int count) {
    return props.entrySet().stream().limit(count).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @BeforeEach
  void setup() {
    dao = new TenantTablePropertiesDao(internalJdbi);
  }

  @Test
  void readWrite() {
    final TenantTableIdentifier identifier = randomIdentifier();
    final Map<String, String> props = randomProperties(3);
    dao.write(identifier, props);
    final Map<String, String> result = dao.read(identifier);
    assertThat(result).isEqualTo(props);
  }

  @Test
  void readWrite_updatedProperties() {
    final TenantTableIdentifier identifier = randomIdentifier();
    final Map<String, String> props1 = randomProperties(3);
    dao.write(identifier, props1);
    final Map<String, String> props2 = randomProperties(4);
    dao.write(identifier, props2);
    final Map<String, String> result = dao.read(identifier);
    assertThat(result).isEqualTo(props2);
  }

  @Test
  void readWrite_updatedMixedProperties() {
    final TenantTableIdentifier identifier = randomIdentifier();
    final Map<String, String> props1 = randomProperties(4);
    dao.write(identifier, props1);
    final Map<String, String> props1Reduce = limit(props1, 2);
    final Map<String, String> props2 = randomProperties(2);
    final ImmutableMap.Builder<String, String> props3Builder = ImmutableMap.<String, String>builder()
        .putAll(props1Reduce).putAll(props2);
    final Map<String, String> props3 = props3Builder.build();
    dao.write(identifier, props3);
    final Map<String, String> result = dao.read(identifier);
    assertThat(result).isEqualTo(props3);
  }

}