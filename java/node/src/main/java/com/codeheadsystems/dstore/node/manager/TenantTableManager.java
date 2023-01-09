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

package com.codeheadsystems.dstore.node.manager;

import com.codeheadsystems.dstore.node.dao.TenantTableDao;
import com.codeheadsystems.dstore.node.model.ImmutableTenantTable;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.metrics.Metrics;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages tenant tables.
 */
@Singleton
public class TenantTableManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableManager.class);

  private final Metrics metrics;
  private final TenantTableDao dao;

  /**
   * Default constructor.
   *
   * @param metrics to use.
   * @param dao     to use.
   */
  @Inject
  public TenantTableManager(final Metrics metrics,
                            final TenantTableDao dao) {
    LOGGER.info("TenantManager({},{})", metrics, dao);
    this.metrics = metrics;
    this.dao = dao;
  }

  /**
   * Gets the current tenant table if it exists.
   *
   * @param tenantId  to get.
   * @param tableName to get.
   * @return the tenant.
   */
  public Optional<TenantTable> get(final String tenantId, final String tableName) {
    LOGGER.debug("get({},{})", tenantId, tableName);
    return metrics.time("TenantTableManager.get", () -> dao.read(tenantId, tableName));
  }

  /**
   * Created the tenant table. If it already exists, simply return the one we already have. Idempotent. Does not set the
   * hash values.
   *
   * @param tenantId  tenant to create.
   * @param tableName table name to create.
   * @return a tenant.
   */
  public TenantTable create(final String tenantId, final String tableName) {
    LOGGER.debug("create({})", tenantId);
    return get(tenantId, tableName).orElseGet(() ->
        metrics.time("TenantTableManager.create", () -> dao.create(buildTenantTable(tenantId, tableName))));
  }

  private TenantTable buildTenantTable(final String tenantId, final String tableName) {
    LOGGER.debug("buildTenantTable({}, {})", tenantId, tableName);
    return ImmutableTenantTable.builder()
        .tenantId(tenantId)
        .tableName(tableName)
        .enabled(true)
        .estimatedQuantity(0)
        .build();
  }

  /**
   * Lists current tables for the tenant.
   *
   * @param tenantId tenant id to look up.
   * @return a list of tables.
   */
  public List<String> tables(final String tenantId) {
    LOGGER.debug("tables()");
    return metrics.time("TenantTableManager.tenants", () -> dao.allTenantTables(tenantId));
  }

  /**
   * Deletes the table. This will destroy the table as well.
   * TODO: delete the actual table.
   *
   * @param tenantId  to delete.
   * @param tableName tableName to delete.
   * @return boolean if deleted or not.
   */
  public boolean delete(final String tenantId, final String tableName) {
    LOGGER.debug("delete({},{})", tenantId, tableName);
    return metrics.time("TenantTableManager.tenants", () -> dao.delete(tenantId, tableName));
  }

}
