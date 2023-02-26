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

package org.svarm.node.engine.impl;

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
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.engine.JsonEngine;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.api.ImmutableEntryInfo;
import org.svarm.node.engine.TableDefinitionEngine;
import org.svarm.node.manager.TenantTableDataSourceManager;
import org.svarm.node.model.TenantTable;

/**
 * First implementation of reading/writing the data for an entry.
 */
@Singleton
public class V1SingleEntryEngine implements TableDefinitionEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(V1SingleEntryEngine.class);
  private static final String INTEGER_TYPE = "INTEGER";
  private static final String STRING_TYPE = "STRING";
  private final Metrics metrics;
  private final TenantTableDataSourceManager dataSourceManager;
  private final JsonEngine jsonEngine;

  /**
   * Default constructor.
   *
   * @param metrics           for analytics.
   * @param dataSourceManager for retrieving data sources of tenant dbs
   * @param jsonEngine        for managing json.
   */
  @Inject
  public V1SingleEntryEngine(final Metrics metrics,
                             final TenantTableDataSourceManager dataSourceManager,
                             final JsonEngine jsonEngine) {
    this.dataSourceManager = dataSourceManager;
    this.jsonEngine = jsonEngine;
    this.metrics = metrics;
    LOGGER.info("V1SingleEntryEngine({},{})", metrics, dataSourceManager);
  }

  /**
   * Reads the entity from the table, returning the JsonNode.
   *
   * @param tenantTable table to read from.
   * @param entity      the entity id.
   * @return an entry if found.
   */
  @Override
  public Optional<EntryInfo> read(final TenantTable tenantTable, final String entity) {
    LOGGER.trace("read({},{})", tenantTable, entity);

    return Jdbi.create(dataSourceManager.getDataSource(tenantTable))
        .withHandle(handle ->
            handle.createQuery("select * from TENANT_DATA where ID = :id")
                .bind("id", entity)
                .scanResultSet(new EntryInfoResultSetScanner()));
  }

  /**
   * Writes the entity to the table.
   *
   * @param tenantTable table to write to.
   * @param entryInfo   the data.
   */
  @Override
  public void write(final TenantTable tenantTable, final EntryInfo entryInfo) {
    LOGGER.trace("write({},{})", tenantTable, entryInfo);
    Jdbi.create(dataSourceManager.getDataSource(tenantTable))
        .withHandle(handle -> {
          PreparedBatch batch = handle.prepareBatch("insert into TENANT_DATA (ID,C_COL,HASH,C_DATA_TYPE,C_DATA,TIMESTAMP) values (:id, :col, :hash, :dataType, :data, :timestamp)");

          entryInfo.data().fieldNames().forEachRemaining(col -> {
            JsonNode element = entryInfo.data().get(col);

            String dataType;

            if (element.isNumber()) {
              dataType = INTEGER_TYPE;
            } else if (element.isTextual()) {
              dataType = STRING_TYPE;
            } else {
              throw new IllegalArgumentException("Unknown type: " + element.getNodeType());
            }

            batch
                .bind("id", entryInfo.id())
                .bind("hash", entryInfo.locationHash())
                .bind("timestamp", entryInfo.timestamp())

                .bind("col", col)
                .bind("dataType", dataType)
                .bind("data", element.asText())
                .add();
          });

          return batch.execute();
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
    LOGGER.trace("delete({},{})", tenantTable, entity);

    final int updateCount = Jdbi.create(dataSourceManager.getDataSource(tenantTable))
        .withHandle(handle -> handle.createUpdate("delete from TENANT_DATA where ID = :id")
            .bind("id", entity)
            .execute()
        );

    final boolean result = updateCount > 0;

    LOGGER.trace("deleted: {}:{}:{}", tenantTable, entity, result);
    return result;
  }

  private class EntryInfoResultSetScanner implements ResultSetScanner<Optional<EntryInfo>> {
    @Override
    public Optional<EntryInfo> scanResultSet(final Supplier<ResultSet> resultSetSupplier, final StatementContext ctx) throws SQLException {
      ResultSet rs = resultSetSupplier.get();

      int rows = 0;
      final ObjectNode node = jsonEngine.createObjectNode();
      long timestamp = 0;
      int hash = 0;
      String entity = null;

      while (rs.next()) {
        rows++;

        if (rs.isFirst()) {
          hash = rs.getInt("HASH");
          entity = rs.getString("ID");
        }

        timestamp = Math.max(timestamp, rs.getLong("TIMESTAMP"));

        final String col = rs.getString("C_COL");
        final String type = rs.getString("C_DATA_TYPE");
        final String data = rs.getString("C_DATA");

        switch (type) {
          case INTEGER_TYPE -> node.put(col, Integer.valueOf(data));
          case STRING_TYPE -> node.put(col, data);
          default -> throw new IllegalArgumentException("Unknown type: " + type);
        }
      }

      if (rows > 0) {
        return Optional.of(ImmutableEntryInfo.builder()
            .data(node)
            .timestamp(timestamp)
            .locationHash(hash)
            .id(entity)
            .build());
      } else {
        return Optional.empty();
      }
    }
  }
}
