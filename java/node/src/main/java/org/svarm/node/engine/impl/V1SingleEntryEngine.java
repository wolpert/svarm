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
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.converter.V1RowConverter;
import org.svarm.node.engine.TableDefinitionEngine;
import org.svarm.node.manager.TenantTableJdbiManager;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.V1Row;

/**
 * First implementation of reading/writing the data for an entry.
 */
@Singleton
public class V1SingleEntryEngine implements TableDefinitionEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(V1SingleEntryEngine.class);
  private static final String INTEGER_TYPE = "INTEGER";
  private static final String STRING_TYPE = "STRING";
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
            handle.createQuery("select * from TENANT_DATA where ID = :id")
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
    dataSourceManager.getJdbi(tenantTable)
        .useTransaction(handle -> {
          PreparedBatch batch = handle.prepareBatch(
              "insert into TENANT_DATA (ID,C_COL,HASH,C_DATA_TYPE,C_DATA,TIMESTAMP) "
                  + "values (:id, :cCol, :hash, :cDataType, :cData, :timestamp)");
          converter.toV1Rows(entryInfo).forEach(row -> batch.bindPojo(row).add());
          batch.execute();
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

    final int updateCount = dataSourceManager.getJdbi(tenantTable)
        .withHandle(handle -> handle.createUpdate("delete from TENANT_DATA where ID = :id")
            .bind("id", entity)
            .execute()
        );

    final boolean result = updateCount > 0;

    LOGGER.trace("deleted: {}:{}:{}", tenantTable, entity, result);
    return result;
  }

}
