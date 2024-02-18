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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.engine.TableDefinitionEngine;
import org.svarm.node.manager.TenantTableJdbiManager;
import org.svarm.node.model.DataStoreActions;
import org.svarm.node.model.ImmutableDataStoreActions;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TombstoneConfiguration;

/**
 * First implementation of reading/writing the data for an entry.
 */
@Singleton
public class V1SingleEntryEngine implements TableDefinitionEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(V1SingleEntryEngine.class);
  private final Metrics metrics;
  private final TenantTableJdbiManager dataSourceManager;
  private final V1RowConverter converter;
  private final Duration expiryDuration;

  /**
   * Default constructor.
   *
   * @param metrics                for analytics.
   * @param dataSourceManager      for retrieving data sources of tenant dbs
   * @param converter              for conversion.
   * @param tombstoneConfiguration the node configuration
   */
  @Inject
  public V1SingleEntryEngine(final Metrics metrics,
                             final TenantTableJdbiManager dataSourceManager,
                             final V1RowConverter converter,
                             final TombstoneConfiguration tombstoneConfiguration) {
    this.dataSourceManager = dataSourceManager;
    this.metrics = metrics;
    this.converter = converter;
    this.expiryDuration = tombstoneConfiguration.expiryDuration();
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
    final List<V1Row> rows = dataSourceManager.getV1RowDao(tenantTable)
        .readEntry(entity);
    if (rows.isEmpty()) {
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
    final V1RowDao dao = dataSourceManager.getV1RowDao(tenantTable);
    dao.useTransaction(handle -> {
      if (!actions.insert().isEmpty()) {
        dao.batchInsert(actions.insert());
      }
      if (!actions.update().isEmpty()) {
        dao.batchUpdate(actions.update());
      }
      if (!actions.delete().isEmpty()) {
        final long expiry = System.currentTimeMillis() + expiryDuration.toMillis();
        final long timestamp = System.currentTimeMillis();
        dao.batchSoftDelete(entryInfo.id(), expiry, timestamp, actions.delete());
        handle.commit();
      }
    });
  }

  /**
   * Generate data store actions.
   *
   * @param v1map        the v 1 map
   * @param existingKeys the existing keys
   * @return the data store actions
   */
  DataStoreActions<V1Row, String> generate(final Map<String, V1Row> v1map, List<String> existingKeys) {
    final ImmutableDataStoreActions.Builder<V1Row, String> builder = ImmutableDataStoreActions.builder();
    v1map.forEach((key, value) -> {
      if (existingKeys.contains(key)) {
        builder.addUpdate(value);
      } else {
        builder.addInsert(value);
      }
    });
    final Set<String> incomingKeys = v1map.keySet();
    builder.addAllDelete(existingKeys.stream().filter(s -> !incomingKeys.contains(s)).toList());
    return builder.build();
  }

  /**
   * Keys list.
   *
   * @param tenantTable the tenant table
   * @param entity      the entity
   * @return the list
   */
  List<String> keys(final TenantTable tenantTable, final String entity) {
    LOGGER.trace("keys({},{})", tenantTable, entity);
    return dataSourceManager.getV1RowDao(tenantTable).keys(entity);
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

    final int updateCount = dataSourceManager.getV1RowDao(tenantTable).delete(entity);
    final boolean result = updateCount > 0;

    LOGGER.trace("deleted: {}:{}:{}", tenantTable, entity, result);
    return result;
  }

  @Override
  public int clearTombstones(final TenantTable tenantTable) {
    LOGGER.trace("clearTombstones()");
    final int count = dataSourceManager.getV1RowDao(tenantTable).deleteExpired(System.currentTimeMillis());

    LOGGER.trace("cleared: {}:{}", tenantTable, count);
    return count;
  }


}
