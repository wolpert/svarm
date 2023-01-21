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

import com.codeheadsystems.dstore.node.model.ImmutableTenantTable;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accessor to tenant table records in the node.
 */
@Singleton
public class TenantTableDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableDao.class);

  private final Jdbi internalJdbi;

  /**
   * Default constructor.
   *
   * @param internalJdbi to execute sql.
   */
  @Inject
  public TenantTableDao(final Jdbi internalJdbi) {
    LOGGER.info("TenantDao({})", internalJdbi);
    this.internalJdbi = internalJdbi;
  }

  /**
   * Creates the tenant table in the database. If it already exists, does nothing but returns the existing tenant.
   *
   * @param tenantTable to create.
   * @return The tenant... either the one that was created or the existing one.
   */
  public TenantTable create(final TenantTable tenantTable) {
    LOGGER.trace("create({})", tenantTable);

    return internalJdbi.withHandle(handle -> {
      int updateCount = handle.createUpdate(
              "insert into NODE_TENANT_TABLES (RID_TENANT,TABLE_NAME,HASH_START,HASH_END, QUANTITY_EST, ENABLED, TABLE_VERSION, KEY, NONCE, PRIMARY_KEY) "
                  + "values (:identifier.tenantId,:identifier.tableName,:hashStart,:hashEnd,:estimatedQuantity,:enabled,:tableVersion,:key,:nonce,:primaryKey)"
          )
          .bindPojo(tenantTable, TenantTable.class)
          .execute();

      if (updateCount != 1) {
        throw new IllegalArgumentException("Unable to create tenant table");
      }
      return tenantTable;
    });
  }

  /**
   * Updates the record with things that can change. (Not the table name or tenant id).
   *
   * @param tenantTable that has the new values.
   * @return the tenant table.
   */
  public TenantTable update(final TenantTable tenantTable) {
    LOGGER.trace("update({})", tenantTable);
    return internalJdbi.withHandle(handle -> {
      int updateCount = handle.createUpdate(
              "update NODE_TENANT_TABLES set "
                  + "HASH_START = :hashStart, "
                  + "HASH_END = :hashEnd, "
                  + "QUANTITY_EST = :estimatedQuantity, "
                  + "ENABLED = :enabled, "
                  + "TABLE_VERSION = :tableVersion, "
                  + "PRIMARY_KEY = :primaryKey "
                  + "where RID_TENANT = :identifier.tenantId "
                  + "and TABLE_NAME = :identifier.tableName "
                  + "and KEY = :key "
                  + "and NONCE = :nonce "
          ).bindPojo(tenantTable)
          .execute();
      if (updateCount == 0) {
        LOGGER.warn("No entry to update for {}", tenantTable);
        throw new IllegalArgumentException("No entry to update");
      } else if (updateCount > 2) {
        LOGGER.error("Oops, this is bad. More than one entry updated. {}", tenantTable);
        throw new IllegalStateException("Multiple entries updated: " + updateCount);
      }
      return tenantTable;
    });
  }

  /**
   * Reads from the current database if the tenant table exists.
   *
   * @param tenantId  tenant to read.
   * @param tableName to read.
   * @return optional tenant if it exists.
   */
  public Optional<TenantTable> read(final String tenantId, final String tableName) {
    LOGGER.trace("read({},{})", tenantId, tableName);
    return internalJdbi.withHandle(handle -> handle
        .createQuery("select * from NODE_TENANT_TABLES where RID_TENANT = :tenant_id and TABLE_NAME = :table_name")
        .bind("tenant_id", tenantId)
        .bind("table_name", tableName)
        .map(new TenantTableRowMapper())
        .findOne()
    );
  }

  /**
   * Returns a list of all tenants in the database.
   *
   * @param tenantId the look for
   * @return list ot tenant ids.
   */
  public List<String> allTenantTables(final String tenantId) {
    LOGGER.trace("allTenantTables({})", tenantId);
    return internalJdbi.withHandle(handle -> handle
        .createQuery("select TABLE_NAME from NODE_TENANT_TABLES where RID_TENANT = :tenant_id")
        .bind("tenant_id", tenantId)
        .mapTo(String.class)
        .list());
  }

  /**
   * Deletes the tenant from the database. If there was no tenant, does nothing.
   *
   * @param tenantId  to delete from.
   * @param tableName to delete.
   * @return boolean if anyone was found to delete.
   */
  public boolean delete(final String tenantId, final String tableName) {
    LOGGER.trace("delete({})", tenantId);
    return internalJdbi.withHandle(handle -> handle
        .createUpdate("delete from NODE_TENANT_TABLES where RID_TENANT = :tenant_id and TABLE_NAME = :table_name")
        .bind("tenant_id", tenantId)
        .bind("table_name", tableName)
        .execute() > 0
    );
  }

  private static class TenantTableRowMapper implements RowMapper<TenantTable> {

    @Override
    public TenantTable map(final ResultSet rs, final StatementContext ctx) throws SQLException {
      return ImmutableTenantTable.builder()
          .identifier(TenantTableIdentifier.from(
              rs.getString("RID_TENANT"),
              rs.getString("TABLE_NAME")))
          .hashStart(Optional.ofNullable(rs.getString("HASH_START")))
          .hashEnd(Optional.ofNullable(rs.getString("HASH_END")))
          .estimatedQuantity(rs.getInt("QUANTITY_EST"))
          .enabled(rs.getBoolean("ENABLED"))
          .tableVersion(rs.getString("TABLE_VERSION"))
          .key(rs.getString("KEY"))
          .nonce(rs.getString("NONCE"))
          .primaryKey(rs.getString("PRIMARY_KEY"))
          .build();
    }
  }
}
