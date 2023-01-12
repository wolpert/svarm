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
import com.codeheadsystems.dstore.node.model.ImmutableTenantTable;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accessor to tenant table records in the node.
 */
@Singleton
public class TenantTableDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableDao.class);

  private final SqlEngine sqlEngine;

  /**
   * Default constructor.
   *
   * @param sqlEngine to execute sql.
   */
  @Inject
  public TenantTableDao(final SqlEngine sqlEngine) {
    LOGGER.info("TenantDao({})", sqlEngine);
    this.sqlEngine = sqlEngine;
  }

  /**
   * Creates the tenant tablein the database. If it already exists, does nothing but returns the existing tenant.
   *
   * @param tenantTable to create.
   * @return The tenant... either the one that was created or the existing one.
   */
  public TenantTable create(final TenantTable tenantTable) {
    LOGGER.debug("create({})", tenantTable);
    sqlEngine.executePreparedInternal(
        "insert into NODE_TENANT_TABLES (RID_TENANT,TABLE_NAME,HASH_START,HASH_END, QUANTITY_EST, ENABLED, TABLE_VERSION, KEY, NONCE) values (?,?,?,?,?,?,?,?,?)",
        (ps) -> {
          try {
            ps.setString(1, tenantTable.tenantId());
            ps.setString(2, tenantTable.tableName());
            sqlEngine.setStringField(3, tenantTable.hashStart(), ps);
            sqlEngine.setStringField(4, tenantTable.hashEnd(), ps);
            ps.setInt(5, tenantTable.estimatedQuantity());
            ps.setBoolean(6, tenantTable.enabled());
            ps.setString(7, tenantTable.tableVersion());
            ps.setString(8, tenantTable.key());
            ps.setString(9, tenantTable.nonce());
            ps.execute();
            if (ps.getUpdateCount() != 1) {
              throw new IllegalArgumentException("Unable to create tenant table");
            }
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to read a tenant table", e);
          }
          return null;
        });
    return tenantTable;
  }

  /**
   * Updates the record with things that can change. (Not the table name or tenant id).
   *
   * @param tenantTable that has the new values.
   * @return the tenant table.
   */
  public TenantTable update(final TenantTable tenantTable) {
    LOGGER.debug("update({})", tenantTable);
    sqlEngine.executePreparedInternal(
        "update NODE_TENANT_TABLES set HASH_START = ?, HASH_END = ?, QUANTITY_EST = ?, ENABLED = ?, TABLE_VERSION = ? where RID_TENANT = ? and TABLE_NAME = ? and KEY = ? and NONCE = ?",
        (ps) -> {
          try {
            sqlEngine.setStringField(1, tenantTable.hashStart(), ps);
            sqlEngine.setStringField(2, tenantTable.hashEnd(), ps);
            ps.setInt(3, tenantTable.estimatedQuantity());
            ps.setBoolean(4, tenantTable.enabled());
            ps.setString(5, tenantTable.tableVersion());
            ps.setString(6, tenantTable.tenantId());
            ps.setString(7, tenantTable.tableName());
            ps.setString(8, tenantTable.key());
            ps.setString(9, tenantTable.nonce());
            ps.execute();
            if (ps.getUpdateCount() == 0) {
              LOGGER.warn("No entry to update for {}", tenantTable);
              throw new IllegalArgumentException("No entry to update");
            } else if (ps.getUpdateCount() > 2) {
              LOGGER.error("Oops, this is bad. More than one entry updated. {}", tenantTable);
              throw new IllegalStateException("Multiple entries updated: " + ps.getUpdateCount());
            }
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to read a tenant table", e);
          }
          return null;
        });
    return tenantTable;
  }

  /**
   * Reads a tenant from the database result set. Does not advance the cursor.
   *
   * @param rs result set to read from. Must be on a row.
   * @return a tenant.
   */
  private TenantTable fromResultSet(final ResultSet rs) {
    try {
      return ImmutableTenantTable.builder()
          .tenantId(rs.getString("RID_TENANT"))
          .tableName(rs.getString("TABLE_NAME"))
          .hashStart(Optional.ofNullable(rs.getString("HASH_START")))
          .hashEnd(Optional.ofNullable(rs.getString("HASH_END")))
          .estimatedQuantity(rs.getInt("QUANTITY_EST"))
          .enabled(rs.getBoolean("ENABLED"))
          .tableVersion("HASH_ALGO")
          .key("KEY")
          .nonce("NONCE")
          .build();
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to read tenant table", e);
    }
  }

  /**
   * Reads from the current database, if the tenant table exists.
   *
   * @param tenantId  tenant to read.
   * @param tableName to read.
   * @return optional tenant if it exists.
   */
  public Optional<TenantTable> read(final String tenantId, final String tableName) {
    LOGGER.debug("read({},{})", tenantId, tableName);
    return sqlEngine.executePreparedInternal(
        "select * from NODE_TENANT_TABLES where RID_TENANT = ? and TABLE_NAME = ?",
        (ps) -> {
          try {
            ps.setString(1, tenantId);
            ps.setString(2, tableName);
            try (final ResultSet rs = ps.executeQuery()) {
              if (rs.next()) {
                return Optional.of(fromResultSet(rs));
              } else {
                return Optional.empty();
              }
            }
          } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to read a tenant table", e);
          }
        });
  }

  /**
   * Returns a list of all tenants in the database.
   *
   * @param tenantId the look for
   * @return list ot tenant ids.
   */
  public List<String> allTenantTables(final String tenantId) {
    LOGGER.debug("allTenantTables({})", tenantId);
    return sqlEngine.executePreparedInternal("select TABLE_NAME from NODE_TENANT_TABLES where RID_TENANT = ?", (ps) -> {
      try {
        ps.setString(1, tenantId);
        try (final ResultSet rs = ps.executeQuery()) {
          final ImmutableList.Builder<String> builder = ImmutableList.builder();
          while (rs.next()) {
            builder.add(rs.getString(1));
          }
          return builder.build();
        }
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to list tenants", e);
      }
    });
  }

  /**
   * Deletes the tenant from the database. If there was no tenant, does nothing.
   *
   * @param tenantId  to delete from.
   * @param tableName to delete.
   * @return boolean if anyone was found to delete.
   */
  public boolean delete(final String tenantId, final String tableName) {
    LOGGER.debug("delete({})", tenantId);
    return sqlEngine.executePreparedInternal("delete from NODE_TENANT_TABLES where RID_TENANT = ? and TABLE_NAME = ?", (ps) -> {
      try {
        ps.setString(1, tenantId);
        ps.setString(2, tableName);
        ps.execute();
        return ps.getUpdateCount() > 0;
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to delete a tenant", e);
      }
    });
  }
}
