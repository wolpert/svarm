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

package com.codeheadsystems.dstore.control.dao;

import com.codeheadsystems.dstore.control.model.NodeRange;
import java.util.List;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;

/**
 * DAO for the Node range.
 */
public interface NodeRangeDao extends Transactional {

  /**
   * Inserts a Node range by the values.
   *
   * @param instance to use.
   */
  @SqlUpdate("insert into NODE_RANGE "
      + "(uuid,tenant,resource, create_date, update_date, table_version, ready, status, low_hash, high_hash) "
      + "values "
      + "(:uuid, :tenant, :resource, :createDate, :updateDate, :tableVersion, :ready, :status, :lowHash, :highHash)")
  void insert(@BindPojo final NodeRange instance);

  /**
   * Updates a Node range by the values.
   *
   * @param instance to use.
   */
  @SqlUpdate("update NODE_RANGE set "
      + "update_date = :updateDate, "
      + "ready = :ready, "
      + "status = :status, "
      + "low_hash = :lowHash, "
      + "high_hash = :highHash "
      + "where uuid = :uuid and tenant = :tenant and resource = :resource")
  void update(@BindPojo final NodeRange instance);

  /**
   * Get the entry from the datastore.
   *
   * @param uuid     to use.
   * @param tenant   to use.
   * @param resource to use.
   * @return the node range.
   */
  @SqlQuery("select * from NODE_RANGE where uuid = :uuid and tenant = :tenant and resource = :resource")
  NodeRange read(@Bind("uuid") String uuid, @Bind("tenant") String tenant, @Bind("resource") String resource);

  /**
   * Get the node ranges from the datastore for the tenant/resource.
   *
   * @param tenant   to use.
   * @param resource to use.
   * @return the list of node ranges.
   */
  @SqlQuery("select * from NODE_RANGE where tenant = :tenant and resource = :resource")
  List<NodeRange> nodeRanges(@Bind("tenant") String tenant, @Bind("resource") String resource);

  /**
   * List all node ranges for a node.
   *
   * @param uuid to use.
   * @return the list of node ranges.
   */
  @SqlQuery("select * from NODE_RANGE where uuid = :uuid")
  List<NodeRange> nodeRanges(@Bind("uuid") String uuid);

  /**
   * List all tenants.
   *
   * @return the tenants.
   */
  @SqlQuery("select distinct(tenant) from NODE_RANGE")
  List<String> tenants();

  /**
   * List all resources for a tenant.
   *
   * @param tenant the tenant
   * @return the resources.
   */
  @SqlQuery("select distinct(resource) from NODE_RANGE where tenant = :tenant")
  List<String> resources(@Bind("tenant") String tenant);

  /**
   * Delete the entry from the database.
   *
   * @param uuid     to delete.
   * @param tenant   to delete.
   * @param resource to delete.
   */
  @SqlUpdate("delete from NODE_RANGE where uuid = :uuid and tenant = :tenant and resource = :resource")
  void delete(@Bind("uuid") String uuid, @Bind("tenant") String tenant, @Bind("resource") String resource);

}
