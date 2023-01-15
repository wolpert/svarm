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

package com.codeheadsystems.dstore.node.engine.impl;

import com.codeheadsystems.dstore.node.engine.SqlEngine;
import com.codeheadsystems.dstore.node.engine.TableDefinitionEngine;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.metrics.Metrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First implementation of reading/writing the data for a entry.
 */
@Singleton
public class V1SingleEntryEngine implements TableDefinitionEngine {

  /**
   * Identifier.
   */
  public static final String DEFINITION_NAME = "V1SingleEntryEngine";
  private static final Logger LOGGER = LoggerFactory.getLogger(V1SingleEntryEngine.class);
  private final Metrics metrics;
  private final SqlEngine sqlEngine;

  /**
   * Default constructor.
   *
   * @param metrics   for analytics.
   * @param sqlEngine to execute sql.
   */
  @Inject
  public V1SingleEntryEngine(final Metrics metrics,
                             final SqlEngine sqlEngine) {
    LOGGER.info("V1SingleEntryEngine({},{})", metrics, sqlEngine);
    this.metrics = metrics;
    this.sqlEngine = sqlEngine;
  }

  /**
   * Uses the murmur3/32b hashing function.
   *
   * @return supplier.
   */
  @Override
  public Supplier<HashFunction> hashFunctionSupplier() {
    return Hashing::murmur3_32_fixed;
  }

  /**
   * Identifier.
   *
   * @return definition name.
   */
  @Override
  public String definitionName() {
    return DEFINITION_NAME;
  }

  /**
   * Reads the entity from the table, returning the JsonNode.
   *
   * @param tenantTable table to read from.
   * @param entity      the entity id.
   * @return a json node if found.
   */
  @Override
  public Optional<JsonNode> read(final TenantTable tenantTable, final String entity) {
    throw new NotImplementedException();
  }

  /**
   * Writes the entity to the table.
   *
   * @param tenantTable table to write to.
   * @param entity      the entity id.
   * @param data        the data.
   */
  @Override
  public void write(final TenantTable tenantTable, final String entity, final JsonNode data) {
    throw new NotImplementedException();
  }

  /**
   * Delete the entity from the table, returning the JsonNode.
   *
   * @param tenantTable table to delete from.
   * @param entity      the entity id.
   * @return boolean if anything was deleted.
   */
  @Override
  public boolean delete(final TenantTable tenantTable, final String entity) {
    throw new NotImplementedException();
  }
}
