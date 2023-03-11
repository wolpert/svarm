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

package org.svarm.node.engine.impl.v1singleentry;

import com.codeheadsystems.metrics.Metrics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.engine.TableDefinitionEngine;
import org.svarm.node.manager.TenantTableJdbiManager;
import org.svarm.node.model.DataStoreActions;
import org.svarm.node.model.ImmutableDataStoreActions;
import org.svarm.node.model.TenantTable;

/**
 * First implementation of reading/writing the data for an entry.
 */
@Singleton
public class V1SingleEntryEngine implements TableDefinitionEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(V1SingleEntryEngine.class);
  private static final String READ_ROWS = "select * from TENANT_DATA where ID = :id";
  private static final String INSERT_ROW = "insert into TENANT_DATA (ID,C_COL,HASH,C_DATA_TYPE,C_DATA,TIMESTAMP) "
      + "values (:id, :cCol, :hash, :cDataType, :cData, :timestamp)";
  private static final String UPDATE_ROW = "update TENANT_DATA set C_DATA_TYPE = :cDataType, C_DATA = :cData, "
      + "TIMESTAMP = :timestamp where ID = :id and C_COL = :cCol";
  private static final String READ_COLUMNS = "select C_COL from TENANT_DATA where :id = id";
  private static final String DELETE_ALL_ROWS_FOR_ENTRY = "delete from TENANT_DATA where ID = :id";
  private static final String DELETE_ONE_ROW_FOR_ENTRY = "delete from TENANT_DATA where ID = :id and C_COL = :cCol";
  private final Metrics metrics;
  private final TenantTableJdbiManager dataSourceManager;
  private final V1RowConverter converter;

  /**
   * Default constructor.
   *
   * @param metrics           for analytics.
   * @param dataSourceManager for retrieving data sources of tenant dbs
   * @param converter         for conversion.
   */
  @Inject
  public V1SingleEntryEngine(final Metrics metrics,
                             final TenantTableJdbiManager dataSourceManager,
                             final V1RowConverter converter) {
    this.dataSourceManager = dataSourceManager;
    this.metrics = metrics;
    this.converter = converter;
    LOGGER.info("V1SingleEntryEngine({},{},{})", metrics, dataSourceManager, converter);
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
    final List<V1Row> rows = dataSourceManager.getJdbi(tenantTable)
        .withHandle(handle ->
            handle.createQuery(READ_ROWS)
                .bind("id", entity)
                .mapTo(V1Row.class)
                .list());
    if (rows.size() == 0) {
      return Optional.empty();
    } else {
      return Optional.of(converter.toEntryInfo(rows));
    }
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
    final Map<String, V1Row> v1Rows = converter.toV1Rows(entryInfo).stream()
        .collect(Collectors.toMap(V1Row::cCol, Function.identity()));
    final List<String> existingKeys = keys(tenantTable, entryInfo.id());
    final DataStoreActions<V1Row, String> actions = generate(v1Rows, existingKeys);
    dataSourceManager.getJdbi(tenantTable)
        .useTransaction(handle -> {
          if (!actions.insert().isEmpty()) {
            final PreparedBatch insertBatch = handle.prepareBatch(INSERT_ROW);
            actions.insert().forEach(row -> insertBatch.bindPojo(row).add());
            insertBatch.execute();
          }
          if (!actions.update().isEmpty()) {
            final PreparedBatch updateBatch = handle.prepareBatch(UPDATE_ROW);
            actions.update().forEach(row -> updateBatch.bindPojo(row).add());
            updateBatch.execute();
          }
          if (!actions.delete().isEmpty()) {
            final PreparedBatch deleteBatch = handle.prepareBatch(DELETE_ONE_ROW_FOR_ENTRY);
            actions.delete().forEach(row -> deleteBatch
                .bind("id", entryInfo.id())
                .bind("cCol", row)
                .add());
            deleteBatch.execute();
          }
        });
  }

  DataStoreActions<V1Row, String> generate(final Map<String, V1Row> v1map, List<String> existingKeys) {
    final ImmutableDataStoreActions.Builder<V1Row, String> builder = ImmutableDataStoreActions.builder();
    v1map.entrySet().forEach(es -> {
      if (existingKeys.contains(es.getKey())) {
        builder.addUpdate(es.getValue());
      } else {
        builder.addInsert(es.getValue());
      }
    });
    final Set<String> incomingKeys = v1map.keySet();
    builder.addAllDelete(existingKeys.stream().filter(s -> !incomingKeys.contains(s)).toList());
    return builder.build();
  }

  List<String> keys(final TenantTable tenantTable, final String entity) {
    LOGGER.trace("keys({},{})", tenantTable, entity);
    return dataSourceManager.getJdbi(tenantTable).withHandle(handle ->
        handle.createQuery(READ_COLUMNS)
            .bind("id", entity)
            .mapTo(String.class)
            .list()
    );
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

    final int updateCount = dataSourceManager.getJdbi(tenantTable)
        .withHandle(handle -> handle.createUpdate(DELETE_ALL_ROWS_FOR_ENTRY)
            .bind("id", entity)
            .execute()
        );

    final boolean result = updateCount > 0;

    LOGGER.trace("deleted: {}:{}:{}", tenantTable, entity, result);
    return result;
  }


}
