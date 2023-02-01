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

package com.codeheadsystems.dstore.control.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.codeheadsystems.dstore.control.manager.NodeRangeManager;
import com.codeheadsystems.dstore.node.api.NodeTenantTableService;
import com.codeheadsystems.dstore.node.api.TenantTableInfo;
import com.codeheadsystems.server.resource.JerseyResource;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the base services for tenant tables, but from the control plane perspective..
 */
@Singleton
public class NodeTenantTableResource implements NodeTenantTableService, JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeTenantTableResource.class);

  private final NodeRangeManager nodeRangeManager;

  /**
   * Constructor.
   *
   * @param nodeRangeManager for node ranges.
   */
  @Inject
  public NodeTenantTableResource(final NodeRangeManager nodeRangeManager) {
    LOGGER.info("NodeTenantTableResource({})", nodeRangeManager);
    this.nodeRangeManager = nodeRangeManager;
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public List<String> listTenantTables(final String tenantId) {
    LOGGER.trace("listTenantTables({})", tenantId);
    return nodeRangeManager.resources(tenantId);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public Optional<TenantTableInfo> readTenantTable(final String tenantId, final String table) {
    LOGGER.trace("readTenantTable({},{})", tenantId, table);
    return Optional.empty();
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public TenantTableInfo createTenantTable(final String tenantId, final String table) {
    LOGGER.trace("createTenantTable({},{})", tenantId, table);
    return null;
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public void deleteTenantTable(final String tenantId, final String table) {
    LOGGER.trace("deleteTenantTable({},{})", tenantId, table);

  }
}
