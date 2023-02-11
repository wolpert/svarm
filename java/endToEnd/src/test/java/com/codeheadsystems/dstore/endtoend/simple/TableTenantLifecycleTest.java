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

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import com.codeheadsystems.dstore.control.javaclient.ControlServiceComponent;
import com.codeheadsystems.dstore.control.javaclient.DaggerControlServiceComponent;
import com.codeheadsystems.dstore.control.javaclient.module.ControlServiceModule;
import com.codeheadsystems.dstore.endtoend.EnvironmentManager;
import com.codeheadsystems.dstore.node.api.NodeTenantTableService;
import com.codeheadsystems.dstore.node.api.TenantTableInfo;
import io.etcd.jetcd.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class TableTenantLifecycleTest {
  private static final Logger LOGGER = getLogger(TableTenantLifecycleTest.class);

  private static final String TABLE = "TableTenantLifecycleTest.table";
  private static final String TENANT = "TableTenantLifecycleTest.tenant";


  @Test
  void createTable() {
    final TenantTableInfo info = EnvironmentManager.COMPONENT.nodeTenantTableService()
        .createTenantTable(TENANT, TABLE);
    LOGGER.info("Create table {} ", info);

  }

}
