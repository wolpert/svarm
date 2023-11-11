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

package org.svarm.control.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import jakarta.ws.rs.NotAcceptableException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.control.common.api.ControlTenantResourceService;
import org.svarm.control.common.api.ResourceMetaData;
import org.svarm.control.common.api.TableMetaData;
import org.svarm.control.common.api.TenantResourceInfo;
import org.svarm.control.converter.TenantResourceInfoConverter;
import org.svarm.control.manager.NodeRangeManager;
import org.svarm.control.model.NodeRange;
import org.svarm.server.resource.JerseyResource;

/**
 * Provides the base services for tenant tables, but from the control plane perspective..
 */
@Singleton
public class NodeTenantTableResource implements ControlTenantResourceService, JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeTenantTableResource.class);

  private final NodeRangeManager nodeRangeManager;
  private final TenantResourceInfoConverter tenantResourceInfoConverter;

  /**
   * Constructor.
   *
   * @param nodeRangeManager            for node ranges.
   * @param tenantResourceInfoConverter for conversion.
   */
  @Inject
  public NodeTenantTableResource(final NodeRangeManager nodeRangeManager,
                                 final TenantResourceInfoConverter tenantResourceInfoConverter) {
    LOGGER.info("NodeTenantTableResource({})", nodeRangeManager);
    this.nodeRangeManager = nodeRangeManager;
    this.tenantResourceInfoConverter = tenantResourceInfoConverter;
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public List<String> listResources(final String tenantId) {
    LOGGER.trace("listTenantTables({})", tenantId);
    return nodeRangeManager.resources(tenantId);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public Optional<TenantResourceInfo> readResource(final String tenantId, final String table) {
    LOGGER.trace("readTenantTable({},{})", tenantId, table);
    final List<NodeRange> nodeRanges = nodeRangeManager.getNodeRange(tenantId, table);
    if (nodeRanges.size() > 0) {
      return Optional.of(tenantResourceInfoConverter.from(nodeRanges));
    } else {
      return Optional.empty();
    }
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public TenantResourceInfo createResource(final String tenantId,
                                           final String table,
                                           final ResourceMetaData resourceMetaData) {
    LOGGER.trace("createTenantTable({},{},{})", tenantId, table, resourceMetaData);
    if (!(resourceMetaData instanceof final TableMetaData tableMetaData)) {
      throw new NotAcceptableException("Only tables suppoerted");
    }
    final List<NodeRange> nodeRanges = nodeRangeManager
        .createTenantResource(tenantId, table, tableMetaData.tableDefinition());
    return tenantResourceInfoConverter.from(nodeRanges);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public void deleteResource(final String tenantId, final String table) {
    LOGGER.trace("deleteResource({},{})", tenantId, table);
    nodeRangeManager.deleteTenantResource(tenantId, table);
  }
}
