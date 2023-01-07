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
import com.codeheadsystems.dstore.node.model.ImmutableTenant;
import com.codeheadsystems.dstore.node.model.Tenant;
import com.codeheadsystems.dstore.tinyorm.engine.TinyOrmEngine;
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
 * Accessor to tenant records in the node. This are not the tenant tables, but the tenant itself.
 */
@Singleton
public class TenantDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantDao.class);

  private final SqlEngine sqlEngine;
  private final TinyOrmEngine tinyOrmEngine;

  /**
   * Default constructor.
   *
   * @param sqlEngine     to execute sql.
   * @param tinyOrmEngine to 'make our life simpler'. lol.
   */
  @Inject
  public TenantDao(final SqlEngine sqlEngine,
                   final TinyOrmEngine tinyOrmEngine) {
    this.tinyOrmEngine = tinyOrmEngine;
    LOGGER.info("TenantDao({})", sqlEngine);
    this.sqlEngine = sqlEngine;
  }

  /**
   * Creates the tenant in the database. If it already exists, does nothing but returns the existing tenant.
   *
   * @param tenant to create.
   * @return The tenant... either the one that was created or the existing one.
   */
  public Tenant create(final Tenant tenant) {
    LOGGER.debug("create({})", tenant.id());
    final String insertQuery = tinyOrmEngine.insertQuery(Tenant.class, "NODE_TENANT");
    sqlEngine.executePreparedInternal(insertQuery, tinyOrmEngine.insertPreparedStatement(tenant));
    return tenant;
  }

  /**
   * Reads a tenant from the database result set. Does not advance the cursor.
   *
   * @param rs result set to read from. Must be on a row.
   * @return a tenant.
   */
  private Tenant fromResultSet(final ResultSet rs) {
    try {
      return ImmutableTenant.builder()
          .id(rs.getString("RID_TENANT"))
          .uuid(rs.getString("UUID"))
          .key(rs.getString("KEY"))
          .nonce(rs.getString("NONCE"))
          .build();
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to read tenant", e);
    }
  }

  /**
   * Reads from the current database, if the tenant exists.
   *
   * @param tenantId tenant to read.
   * @return optional tenant if it exists.
   */
  public Optional<Tenant> read(final String tenantId) {
    LOGGER.debug("read({})", tenantId);
    return sqlEngine.executePreparedInternal("select * from NODE_TENANT where RID_TENANT = ?", (ps) -> {
      try {
        ps.setString(1, tenantId);
        try (final ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            return Optional.of(fromResultSet(rs));
          } else {
            return Optional.empty();
          }
        }
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to read a tenant", e);
      }
    });
  }

  /**
   * Returns a list of all tenants in the database.
   *
   * @return list ot tenant ids.
   */
  public List<String> allTenants() {
    LOGGER.debug("allTenants()");
    return sqlEngine.executeQueryInternal("select RID_TENANT from NODE_TENANT", (rs) -> {
      final ImmutableList.Builder<String> builder = ImmutableList.builder();
      try {
        while (rs.next()) {
          builder.add(rs.getString(1));
        }
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to list tenants", e);
      }
      return builder.build();
    });
  }

  /**
   * Deletes the tenant from the database. If there was no tenant, does nothing.
   *
   * @param tenantId to delete.
   * @return boolean if anyone was found to delete.
   */
  public boolean delete(final String tenantId) {
    LOGGER.debug("delete({})", tenantId);
    return sqlEngine.executePreparedInternal("delete from NODE_TENANT where RID_TENANT = ?", (ps) -> {
      try {
        ps.setString(1, tenantId);
        ps.execute();
        return ps.getUpdateCount() > 0;
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to delete a tenant", e);
      }
    });
  }
}
