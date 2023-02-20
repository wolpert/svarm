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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.model.Tenant;

/**
 * Accessor to tenant records in the node. These are not the tenant tables, but the tenant itself.
 */
@Singleton
public class TenantDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantDao.class);

  private final Jdbi internalJdbi;

  /**
   * Default constructor.
   *
   * @param internalJdbi to get the internal datasource for tenant info
   */
  @Inject
  public TenantDao(final Jdbi internalJdbi) {
    LOGGER.trace("TenantDao({})", internalJdbi);
    this.internalJdbi = internalJdbi;
  }

  /**
   * Creates the tenant in the database. If it already exists, it does nothing but returns the existing tenant.
   *
   * @param tenant to create.
   * @return The tenant... either the one that was created or the existing one.
   */
  public Tenant create(final Tenant tenant) {
    LOGGER.trace("create({})", tenant);
    final Integer updateCount = internalJdbi.withHandle(handle ->
        handle.createUpdate("insert into NODE_TENANT (RID_TENANT,UUID,KEY,NONCE) values (:ridTenant, :uuid, :key, :nonce)")
            .bindPojo(tenant)
            .execute()
    );
    if (updateCount != 1) {
      throw new IllegalArgumentException("Unable to create tenant");
    }
    return tenant;
  }

  /**
   * Reads from the current database if the tenant exists.
   *
   * @param tenantId tenant to read.
   * @return optional tenant if it exists.
   */
  public Optional<Tenant> read(final String tenantId) {
    LOGGER.trace("read({})", tenantId);
    return internalJdbi.withHandle(handle ->
        handle.createQuery("select * from NODE_TENANT where RID_TENANT = :tenantId")
            .bind("tenantId", tenantId)
            .mapTo(Tenant.class)
            .findFirst());
  }

  /**
   * Returns a list of all tenants in the database.
   *
   * @return list ot tenant ids.
   */
  public List<String> allTenants() {
    LOGGER.trace("allTenants()");
    return internalJdbi.withHandle(handle ->
        handle.createQuery("select RID_TENANT from NODE_TENANT")
            .mapTo(String.class)
            .list());
  }

  /**
   * Deletes the tenant from the database. If there was no tenant, does nothing.
   *
   * @param tenantId to delete.
   * @return boolean if anyone was found to delete.
   */
  public boolean delete(final String tenantId) {
    LOGGER.trace("delete({})", tenantId);
    return internalJdbi.withHandle(handle ->
        handle.createUpdate("delete from NODE_TENANT where RID_TENANT = :tenantId")
            .bind("tenantId", tenantId)
            .execute() > 0);
  }

}
