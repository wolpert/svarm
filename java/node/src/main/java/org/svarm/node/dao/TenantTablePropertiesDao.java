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

package org.svarm.node.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * Access pattern to the properties of a tenant table.
 */
@Singleton
public class TenantTablePropertiesDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTablePropertiesDao.class);

  private final Jdbi internalJdbi;

  /**
   * Constructor.
   *
   * @param internalJdbi for database queries.
   */
  @Inject
  public TenantTablePropertiesDao(final Jdbi internalJdbi) {
    LOGGER.info("TenantTablePropertiesDao({})", internalJdbi);
    this.internalJdbi = internalJdbi;
  }

  /**
   * Writes the properties to the table. Will remove properties not in the list.
   *
   * @param identifier to update.
   * @param properties the properties.
   */
  public void write(final TenantTableIdentifier identifier,
                    final Map<String, String> properties) {
    // TODO: Should we turn off autocommit?
    LOGGER.trace("write({},{})", identifier, properties.keySet());
    final Map<String, String> existing = read(identifier);

    final Map<String, String> valuesToUpdate = properties.keySet().stream()
        .filter(existing::containsKey)
        .collect(Collectors.toMap(s -> s, properties::get));

    if (!valuesToUpdate.isEmpty()) {
      internalJdbi.withHandle(handle -> {
        final PreparedBatch preparedBatch = handle.prepareBatch("update NODE_TENANT_TABLES_CONFIGURATION set VALUE = :value where RID_TENANT = :tenantId and TABLE_NAME = :tableName and KEY = :key");
        valuesToUpdate.forEach((key, value) -> preparedBatch
            .bindPojo(identifier)
            .bind("key", key)
            .bind("value", value)
            .add());
        return preparedBatch.execute();
      });
    }

    final List<String> toDelete = existing.keySet().stream()
        .filter(k -> !properties.containsKey(k))
        .toList();

    if (toDelete.size() > 0) {
      delete(identifier, toDelete);
    }

    final Map<String, String> valuesToInsert = properties.keySet().stream()
        .filter(k -> !existing.containsKey(k))
        .collect(Collectors.toMap(s -> s, properties::get));
    if (!valuesToInsert.isEmpty()) {
      internalJdbi.withHandle(handle -> {
        PreparedBatch preparedBatch = handle.prepareBatch("insert into NODE_TENANT_TABLES_CONFIGURATION (RID_TENANT,TABLE_NAME,KEY,VALUE) values (:tenantId,:tableName,:key,:value)");

        valuesToInsert.forEach((key, value) -> preparedBatch
            .bindPojo(identifier)
            .bind("key", key)
            .bind("value", value)
            .add());
        return preparedBatch.execute();
      });
    }
  }

  /**
   * Delete all properties of the tenant table identifier.
   *
   * @param identifier to delete.
   */
  public void delete(final TenantTableIdentifier identifier) {
    LOGGER.trace("delete({})", identifier);
    internalJdbi.withHandle(handle -> handle
        .createUpdate("delete from NODE_TENANT_TABLES_CONFIGURATION where RID_TENANT = :tenantId and TABLE_NAME = :tableName")
        .bindPojo(identifier)
        .execute());
  }

  /**
   * Delete listed properties of the tenant table identifier.
   *
   * @param identifier to delete.
   * @param keys       to delete.
   */
  public void delete(final TenantTableIdentifier identifier, final List<String> keys) {
    LOGGER.trace("delete({},{})", identifier, keys);
    internalJdbi.withHandle(handle -> handle
        .createUpdate("delete from NODE_TENANT_TABLES_CONFIGURATION where RID_TENANT = :tenantId and TABLE_NAME = :tableName and KEY in (<keys>)")
        .bindPojo(identifier)
        .bindList("keys", keys)
        .execute());
  }

  /**
   * Reads the current properties.
   *
   * @param identifier to read.
   * @return the properties.
   */
  public Map<String, String> read(final TenantTableIdentifier identifier) {
    LOGGER.trace("read({})", identifier);
    return internalJdbi.withHandle(handle -> handle
        .createQuery("select KEY, VALUE from NODE_TENANT_TABLES_CONFIGURATION where RID_TENANT = :tenantId and TABLE_NAME = :tableName")
        .bindPojo(identifier)
        .reduceRows(new HashMap<>(), (map, rowView) -> {
          map.put(rowView.getColumn("KEY", String.class), rowView.getColumn("VALUE", String.class));
          return map;
        })
    );
  }

}
