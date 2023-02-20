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
import static org.slf4j.LoggerFactory.getLogger;
import static org.svarm.endtoend.EnvironmentManager.COMPONENT;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.svarm.control.common.api.TenantResourceInfo;

public class TableTenantLifecycleTest {
  private static final Logger LOGGER = getLogger(TableTenantLifecycleTest.class);

  private static final String TABLE = "TableTenantLifecycleTest.table";
  private static final String TENANT = "TableTenantLifecycleTest.tenant";
  private static final String ENTRY = "newEntry";

  @AfterEach
  void clearTraceUuid() {
    COMPONENT.traceUuidEngine().clear();
  }

  @Test
  void createTable() throws InterruptedException, IOException {
    COMPONENT.traceUuidEngine().set("TableTenantLifecycleTest.createTable");
    final TenantResourceInfo info = COMPONENT.controlTenantResourceService()
        .createResource(TENANT, TABLE);
    LOGGER.info("Create table {} ", info);
    COMPONENT.etcdAccessor().getAll("node", "").forEach(LOGGER::info);
    JsonNode data = COMPONENT.objectMapper().readValue("{\"a\":2}", JsonNode.class);
    boolean ready = false;
    for (int i = 0; i < 10; i++) {
      ready = COMPONENT.controlTenantResourceService().readResource(TENANT, TABLE).get().ready();
      if (ready) {
        LOGGER.info("Ready in iteration " + i);
        break;
      } else {
        Thread.sleep(100);
      }
    }
    assertThat(ready).isTrue();
    COMPONENT.proxyService().createTenantTableEntry(TENANT, TABLE, ENTRY, data);
    final JsonNode result = COMPONENT.proxyService().readTenantTableEntry(TENANT, TABLE, ENTRY).get();
    assertThat(result).isEqualTo(data);
    COMPONENT.proxyService().deleteTenantTableEntry(TENANT, TABLE, ENTRY);
  }

}
