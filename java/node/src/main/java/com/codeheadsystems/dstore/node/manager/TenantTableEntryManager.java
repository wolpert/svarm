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

package com.codeheadsystems.dstore.node.manager;

import com.codeheadsystems.dstore.node.engine.TableDefinitionEngine;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles reading/writing into the tenant table we are executing.
 */
@Singleton
public class TenantTableEntryManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableEntryManager.class);

  private final Map<String, TableDefinitionEngine> tableDefinitionEngineMap;

  /**
   * Constructor.
   *
   * @param tableDefinitionEngineMap the map of who does the hard work.
   */
  @Inject
  public TenantTableEntryManager(final Map<String, TableDefinitionEngine> tableDefinitionEngineMap) {
    LOGGER.info("TenantTableEntryManager({})", tableDefinitionEngineMap);
    this.tableDefinitionEngineMap = tableDefinitionEngineMap;
  }
}
