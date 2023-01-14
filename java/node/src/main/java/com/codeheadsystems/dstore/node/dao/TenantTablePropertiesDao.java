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

package com.codeheadsystems.dstore.node.dao;

import com.codeheadsystems.dstore.node.engine.SqlEngine;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.google.common.collect.ImmutableMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access pattern to the properties of a tenant table.
 */
@Singleton
public class TenantTablePropertiesDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTablePropertiesDao.class);

  private final SqlEngine sqlEngine;

  /**
   * Constructor.
   *
   * @param sqlEngine for database queries.
   */
  @Inject
  public TenantTablePropertiesDao(final SqlEngine sqlEngine) {
    this.sqlEngine = sqlEngine;
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
    LOGGER.debug("write({},{})", identifier, properties.keySet());
    final Map<String, String> existing = read(identifier);
    final List<String> toDelete = existing.keySet().stream()
        .filter(k -> !properties.containsKey(k))
        .toList();
    final List<String> newValues = properties.keySet().stream()
        .filter(k -> !existing.containsKey(k))
        .toList();
    final Map<String, String> valuesToInsert = newValues.stream().collect(Collectors.toMap(s -> s, properties::get));
    final List<String> updateValues = properties.keySet().stream()
        .filter(k -> existing.containsKey(k))
        .toList();
    final Map<String, String> valuesToUpdate = updateValues.stream().collect(Collectors.toMap(s -> s, properties::get));
    sqlEngine.executePreparedInternal(
        "update NODE_TENANT_TABLES_CONFIGURATION set VALUE = ? where RID_TENANT = ? and TABLE_NAME = ? and KEY = ?",
        (ps) -> {
          try {
            for (Map.Entry<String, String> entry : valuesToInsert.entrySet()) {
              ps.setString(1, entry.getValue());
              ps.setString(2, identifier.tenantId());
              ps.setString(3, identifier.tableName());
              ps.setString(4, entry.getKey());
              ps.addBatch();
            }
            ps.executeBatch();
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to update properties", e);
          }
          return null;
        });
    if (toDelete.size() > 0) {
      delete(identifier, toDelete);
    }
    sqlEngine.executePreparedInternal(
        "insert into NODE_TENANT_TABLES_CONFIGURATION (RID_TENANT,TABLE_NAME,KEY,VALUE) values (?,?,?,?)",
        (ps) -> {
          try {
            for (Map.Entry<String, String> entry : valuesToInsert.entrySet()) {
              ps.setString(1, identifier.tenantId());
              ps.setString(2, identifier.tableName());
              ps.setString(3, entry.getKey());
              ps.setString(4, entry.getValue());
              ps.addBatch();
            }
            ps.executeBatch();
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to delete properties", e);
          }
          return null;
        });
  }

  /**
   * Delete all properties of the tenant table identifier.
   *
   * @param identifier to delete.
   */
  public void delete(final TenantTableIdentifier identifier) {
    LOGGER.debug("delete({})", identifier);
    sqlEngine.executePreparedInternal(
        "delete from NODE_TENANT_TABLES_CONFIGURATION where RID_TENANT = ? and TABLE_NAME = ?",
        (ps) -> {
          try {
            ps.setString(1, identifier.tenantId());
            ps.setString(2, identifier.tableName());
            ps.execute();
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to delete properties", e);
          }
          return null;
        });
  }

  /**
   * Delete listed properties of the tenant table identifier.
   *
   * @param identifier to delete.
   * @param keys       to delete.
   */
  public void delete(final TenantTableIdentifier identifier, final List<String> keys) {
    LOGGER.debug("delete({},{})", identifier, keys);
    final String inClause = keys.stream().map(s -> "?").collect(Collectors.joining(","));
    sqlEngine.executePreparedInternal(
        "delete from NODE_TENANT_TABLES_CONFIGURATION where RID_TENANT = ? and TABLE_NAME = ? and KEY in (" + inClause + ")",
        (ps) -> {
          try {
            ps.setString(1, identifier.tenantId());
            ps.setString(2, identifier.tableName());
            int i = 3;
            for (String key : keys) {
              ps.setString(i++, key);
            }
            ps.execute();
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to delete properties", e);
          }
          return null;
        });
  }

  /**
   * Reads the current properties.
   *
   * @param identifier to read.
   * @return the properties.
   */
  public Map<String, String> read(final TenantTableIdentifier identifier) {
    LOGGER.debug("read({})", identifier);
    final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    sqlEngine.executePreparedInternal(
        "select * from NODE_TENANT_TABLES_CONFIGURATION where RID_TENANT = ? and TABLE_NAME = ?",
        (ps) -> {
          try {
            ps.setString(1, identifier.tenantId());
            ps.setString(2, identifier.tableName());
            try (final ResultSet rs = ps.executeQuery()) {
              while (rs.next()) {
                builder.put(rs.getString("KEY"), rs.getString("VALUE"));
              }
            }
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to read properties", e);
          }
          return null;
        });
    return builder.build();
  }

}
