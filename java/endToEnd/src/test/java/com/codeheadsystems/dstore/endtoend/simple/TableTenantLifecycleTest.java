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

package com.codeheadsystems.dstore.endtoend.simple;

import static com.codeheadsystems.dstore.endtoend.EnvironmentManager.COMPONENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.control.common.api.TenantResourceInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class TableTenantLifecycleTest {
  private static final Logger LOGGER = getLogger(TableTenantLifecycleTest.class);

  private static final String TABLE = "TableTenantLifecycleTest.table";
  private static final String TENANT = "TableTenantLifecycleTest.tenant";

  @AfterEach
  void clearTraceUuid() {
    COMPONENT.traceUuidEngine().clear();
  }

  @Test
  void createTable() throws InterruptedException {
    COMPONENT.traceUuidEngine().set("TableTenantLifecycleTest.createTable");
    final TenantResourceInfo info = COMPONENT.controlTenantResourceService()
        .createResource(TENANT, TABLE);
    LOGGER.info("Create table {} ", info);
    COMPONENT.etcdAccessor().getAll("node", "").forEach(LOGGER::info);
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
  }

}
