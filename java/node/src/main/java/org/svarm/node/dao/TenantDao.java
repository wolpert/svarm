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

import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.svarm.node.model.Tenant;

/**
 * Accessor to tenant records in the node. These are not the tenant tables, but the tenant itself.
 */
public interface TenantDao {

  /**
   * Creates the tenant in the database. If it already exists, it does nothing but returns the existing tenant.
   *
   * @param tenant to create.
   */
  @SqlUpdate("insert into NODE_TENANT (RID_TENANT,UUID,KEY,NONCE) values (:ridTenant, :uuid, :key, :nonce)")
  void create(@BindPojo final Tenant tenant);

  /**
   * Reads from the current database if the tenant exists.
   *
   * @param tenantId tenant to read.
   * @return optional tenant if it exists.
   */
  @SqlQuery("select * from NODE_TENANT where RID_TENANT = :tenantId")
  Optional<Tenant> read(@Bind("tenantId") final String tenantId);

  /**
   * Returns a list of all tenants in the database.
   *
   * @return list ot tenant ids.
   */
  @SqlQuery("select RID_TENANT from NODE_TENANT")
  List<String> allTenants();

  /**
   * Deletes the tenant from the database. If there was no tenant, does nothing.
   *
   * @param tenantId to delete.
   */
  @SqlUpdate("delete from NODE_TENANT where RID_TENANT = :tenantId")
  void delete(@Bind("tenantId") final String tenantId);

}
