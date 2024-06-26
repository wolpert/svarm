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

package org.svarm.endtoend.simple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.slf4j.LoggerFactory.getLogger;
import static org.svarm.endtoend.EnvironmentManager.COMPONENT;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import io.github.resilience4j.feign.FeignDecorator;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.svarm.control.common.api.ImmutableTableMetaData;
import org.svarm.control.common.api.TableMetaData;
import org.svarm.control.common.api.TenantResourceInfo;
import org.svarm.datastore.common.TableDefinition;

public class TableTenantLifecycleTest {
  private static final Logger LOGGER = getLogger(TableTenantLifecycleTest.class);

  private static final String TABLE = "TableTenantLifecycleTest.table";
  private static final String TENANT = "TableTenantLifecycleTest.tenant";
  private static final String ENTRY = "newEntry";
  private static final TableMetaData META_DATA = ImmutableTableMetaData.builder().tableDefinition(TableDefinition.V1SingleEntryEngine)
      .build();

  @AfterEach
  void clearTraceUuid() {
    COMPONENT.traceUuidEngine().clear();
  }

  @Test
  void createTable() throws IOException {
    final String tenant = TENANT + ":" + UUID.randomUUID();
    COMPONENT.traceUuidEngine().set("TableTenantLifecycleTest.createTable");
    final TenantResourceInfo info = COMPONENT.controlTenantResourceService().createResource(tenant, TABLE, META_DATA);
    LOGGER.info("Create table {} ", info);
    COMPONENT.etcdAccessor().getAll("node", "").forEach(LOGGER::info);
    final JsonNode data = COMPONENT.objectMapper().readValue("{\"a\":2,\"something\":\"else\"}", JsonNode.class);
    final JsonNode data2 = COMPONENT.objectMapper().readValue("{\"a\":2,\"b\":\"altogether\"}", JsonNode.class);
    boolean ready = retry(20, () -> COMPONENT.controlTenantResourceService().readResource(tenant, TABLE).get().ready());
    assertThat(ready).isTrue();
    COMPONENT.proxyService().createTenantTableEntry(tenant, TABLE, ENTRY, data);
    final JsonNode result = COMPONENT.proxyService().readTenantTableEntry(tenant, TABLE, ENTRY).get();
    assertThat(result).isEqualTo(data);
    COMPONENT.proxyService().createTenantTableEntry(tenant, TABLE, ENTRY, data2);
    final JsonNode result2 = COMPONENT.proxyService().readTenantTableEntry(tenant, TABLE, ENTRY).get();
    assertThat(result2).isEqualTo(data2);
    COMPONENT.proxyService().deleteTenantTableEntry(tenant, TABLE, ENTRY);
    assertThatExceptionOfType(FeignException.NotFound.class)
        .isThrownBy(() -> COMPONENT.proxyService().readTenantTableEntry(tenant, TABLE, ENTRY));
    COMPONENT.controlTenantResourceService().deleteResource(tenant, TABLE);
    final boolean deleted = retry(20, () -> {
      try {
        return COMPONENT.controlTenantResourceService().readResource(tenant, TABLE).isEmpty();
      } catch (FeignException.NotFound e) {
        return true;
      }
    });
    assertThat(deleted)
        .as("Asserting that the table was deleted")
        .isTrue();
  }

  boolean retry(final int times,
                final Supplier<Boolean> thingToRetry) {
    for (int i = 0; i < times; i++) {
      if (thingToRetry.get()) {
        LOGGER.info("Ready in iteration " + i);
        return true;
      } else {
        LOGGER.info("Failed ready in iteration " + i);
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return false;
  }

}
