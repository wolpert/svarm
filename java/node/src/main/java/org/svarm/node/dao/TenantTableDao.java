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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.svarm.node.model.ImmutableTenantTable;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * Accessor to tenant table records in the node.
 */
public interface TenantTableDao {

  /**
   * Creates the tenant table in the database. If it already exists, does nothing but returns the existing tenant.
   *
   * @param tenantTable to create.
   */
  @SqlUpdate("insert into NODE_TENANT_TABLES "
      + "(RID_TENANT,TABLE_NAME,HASH, QUANTITY_EST, ENABLED, TABLE_VERSION, KEY, NONCE) "
      + "values (:identifier.tenantId,:identifier.tableName,:hash,:estimatedQuantity,:enabled,:tableVersion,:key,:nonce)")
  void create(@BindPojo final TenantTable tenantTable);

  /**
   * Updates the record with things that can change. (Not the table name or tenant id).
   *
   * @param tenantTable that has the new values.
   */
  @SqlUpdate("update NODE_TENANT_TABLES set "
      + "HASH = :hash, "
      + "QUANTITY_EST = :estimatedQuantity, "
      + "ENABLED = :enabled, "
      + "TABLE_VERSION = :tableVersion "
      + "where RID_TENANT = :identifier.tenantId "
      + "and TABLE_NAME = :identifier.tableName "
      + "and KEY = :key "
      + "and NONCE = :nonce ")
  void update(@BindPojo final TenantTable tenantTable);

  /**
   * Reads from the current database if the tenant table exists.
   *
   * @param tenantId  tenant to read.
   * @param tableName to read.
   * @return optional tenant if it exists.
   */
  @SqlQuery("select * from NODE_TENANT_TABLES where RID_TENANT = :tenant_id and TABLE_NAME = :table_name")
  @RegisterRowMapper(TenantTableRowMapper.class)
  Optional<TenantTable> read(@Bind("tenant_id") final String tenantId,
                             @Bind("table_name") final String tableName);

  /**
   * Returns a list of all tenants in the database.
   *
   * @param tenantId the look for
   * @return list ot tenant ids.
   */
  @SqlQuery("select TABLE_NAME from NODE_TENANT_TABLES where RID_TENANT = :tenant_id")
  List<String> allTenantTables(@Bind("tenant_id") final String tenantId);

  /**
   * Deletes the tenant from the database. If there was no tenant, does nothing.
   *
   * @param tenantId  to delete from.
   * @param tableName to delete.
   */
  @SqlUpdate("delete from NODE_TENANT_TABLES where RID_TENANT = :tenant_id and TABLE_NAME = :table_name")
  void delete(@Bind("tenant_id") final String tenantId,
              @Bind("table_name") final String tableName);

  /**
   * Row mapper.
   */
  class TenantTableRowMapper implements RowMapper<TenantTable> {

    @Override
    public TenantTable map(final ResultSet rs, final StatementContext ctx) throws SQLException {
      return ImmutableTenantTable.builder()
          .identifier(TenantTableIdentifier.from(
              rs.getString("RID_TENANT"),
              rs.getString("TABLE_NAME")))
          .hash(Optional.ofNullable(rs.getString("HASH")))
          .estimatedQuantity(rs.getInt("QUANTITY_EST"))
          .enabled(rs.getBoolean("ENABLED"))
          .tableVersion(rs.getString("TABLE_VERSION"))
          .key(rs.getString("KEY"))
          .nonce(rs.getString("NONCE"))
          .build();
    }
  }
}
