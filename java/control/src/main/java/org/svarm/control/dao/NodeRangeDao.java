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

package org.svarm.control.dao;

import java.util.List;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.jdbi.v3.sqlobject.transaction.Transactional;
import org.svarm.control.model.NodeRange;

/**
 * DAO for the Node range.
 */
public interface NodeRangeDao extends Transactional<NodeRangeDao> {

  /**
   * Inserts a Node range by the values.
   *
   * @param instance to use.
   */
  @Transaction
  @SqlUpdate("insert into NODE_RANGE "
      + "(node_uuid,tenant,resource, create_date, update_date, table_version, ready, status, hash) "
      + "values "
      + "(:nodeUuid, :tenant, :resource, :createDate, :updateDate, :tableVersion, :ready, :status, :hash)")
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
      + "hash = :hash "
      + "where node_uuid = :nodeUuid and tenant = :tenant and resource = :resource")
  void update(@BindPojo final NodeRange instance);

  /**
   * Get the entry from the datastore.
   *
   * @param nodeUuid to use.
   * @param tenant   to use.
   * @param resource to use.
   * @return the node range.
   */
  @SqlQuery("select * from NODE_RANGE where node_uuid = :nodeUuid and tenant = :tenant and resource = :resource")
  NodeRange read(@Bind("nodeUuid") String nodeUuid, @Bind("tenant") String tenant, @Bind("resource") String resource);

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
  @SqlQuery("select * from NODE_RANGE where node_uuid = :nodeUuid")
  List<NodeRange> nodeRanges(@Bind("nodeUuid") String uuid);

  /**
   * Get the node ranges from the datastore for the tenant/resource, but using the API version of node ranges.
   * TODO: this is a little janky. Maybe we can figure out a better way.
   *
   * @param tenant   to use.
   * @param resource to use.
   * @return the list of node ranges.
   */
  @SqlQuery("select *, NODES.host || ':' || NODES.port as uri "
      + "from NODE_RANGE, NODES where NODE_RANGE.tenant = :tenant "
      + "and NODE_RANGE.resource = :resource "
      + "and NODE_RANGE.node_uuid = NODES.uuid")
  List<org.svarm.common.config.api.NodeRange> apiNodeRanges(
      @Bind("tenant") String tenant,
      @Bind("resource") String resource);

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
   * @param nodeUuid to delete.
   * @param tenant   to delete.
   * @param resource to delete.
   */
  @SqlUpdate("delete from NODE_RANGE where node_uuid = :nodeUuid and tenant = :tenant and resource = :resource")
  void delete(@Bind("nodeUuid") String nodeUuid, @Bind("tenant") String tenant, @Bind("resource") String resource);

}
