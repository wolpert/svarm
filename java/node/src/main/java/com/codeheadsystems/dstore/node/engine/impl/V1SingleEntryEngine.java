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

import com.codeheadsystems.dstore.common.engine.JsonEngine;
import com.codeheadsystems.dstore.node.engine.SqlEngine;
import com.codeheadsystems.dstore.node.engine.TableDefinitionEngine;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.metrics.Metrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
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
  private static final String INTEGER_TYPE = "INTEGER";
  private static final String STRING_TYPE = "STRING";
  private final Metrics metrics;
  private final SqlEngine sqlEngine;
  private final JsonEngine jsonEngine;

  /**
   * Default constructor.
   *
   * @param metrics    for analytics.
   * @param sqlEngine  to execute sql.NO
   * @param jsonEngine for managing json.
   */
  @Inject
  public V1SingleEntryEngine(final Metrics metrics,
                             final SqlEngine sqlEngine,
                             final JsonEngine jsonEngine) {
    this.jsonEngine = jsonEngine;
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
    LOGGER.trace("read({},{}", tenantTable, entity);
    return sqlEngine.executePreparedTenant(tenantTable,
        "select * from TENANT_DATA where ID = ?",
        (ps) -> {
          try {
            ps.setString(1, entity);
            try (final ResultSet rs = ps.executeQuery()) {
              int rows = 0;
              final ObjectNode node = jsonEngine.createObjectNode();
              while (rs.next()) {
                rows++;
                final String col = rs.getString("C_COL");
                LOGGER.trace("setting {}->{}", entity, col);
                final String type = rs.getString("C_DATA_TYPE");
                final String data = rs.getString("C_DATA");
                switch (type) {
                  case INTEGER_TYPE -> node.put(col, Integer.valueOf(data));
                  case STRING_TYPE -> node.put(col, data);
                  default -> {
                    LOGGER.error("Unknown: {}:{}:{}:{}", tenantTable, entity, col, type);
                    throw new IllegalArgumentException("Unknown type: " + type + " for " + entity + ":" + tenantTable);
                  }
                }
              }
              if (rows > 0) {
                return Optional.of(node);
              } else {
                return Optional.empty();
              }
            }
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to read data for: " + tenantTable + ":" + entity, e);
          }
        });
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
    LOGGER.trace("write({},{}", tenantTable, entity);
    final Integer hash = hashFunctionSupplier().get().hashUnencodedChars(entity).asInt();
    sqlEngine.executePreparedTenant(tenantTable,
        "insert into TENANT_DATA (ID,C_COL,HASH,C_DATA_TYPE,C_DATA) values (?,?,?,?,?)",
        (ps) -> {
          try {
            data.fieldNames().forEachRemaining(col -> {
              try {
                ps.setString(1, entity);
                ps.setString(2, col);
                ps.setInt(3, hash);
                final JsonNode element = data.get(col);
                if (element.isNumber()) {
                  ps.setString(4, INTEGER_TYPE);
                } else if (element.isTextual()) {
                  ps.setString(4, STRING_TYPE);
                } else {
                  throw new IllegalArgumentException("Unknown type: " + element.getNodeType());
                }
                ps.setString(5, element.asText());
                ps.addBatch();
              } catch (SQLException e) {
                LOGGER.error("Unable to add row: {},{}", tenantTable, entity, e);
                throw new IllegalArgumentException("Unable to add row", e);
              }
            });
            ps.executeBatch();
            return null;
          } catch (SQLException e) {
            LOGGER.error("Unable to execute batch: {},{}", tenantTable, entity, e);
            throw new IllegalArgumentException("Unable to execute batch", e);
          }
        });
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
    LOGGER.trace("delete({},{}", tenantTable, entity);
    return sqlEngine.executePreparedTenant(tenantTable,
        "delete from TENANT_DATA where ID = ?",
        (ps) -> {
          try {
            ps.setString(1, entity);
            final int rows = ps.executeUpdate();
            final boolean result = rows > 0;
            LOGGER.trace("deleted: {}:{}:{}", tenantTable, entity, result);
            return result;
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to read data for: " + tenantTable + ":" + entity, e);
          }
        });
  }
}
