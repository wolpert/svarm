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

package com.codeheadsystems.dstore.control.manager;

import com.codeheadsystems.dstore.common.config.api.ImmutableNodeTenantResource;
import com.codeheadsystems.dstore.common.config.api.ImmutableNodeTenantResourceRange;
import com.codeheadsystems.dstore.common.config.api.ImmutableRange;
import com.codeheadsystems.dstore.common.config.api.ImmutableTenantResource;
import com.codeheadsystems.dstore.common.config.api.TenantResource;
import com.codeheadsystems.dstore.common.config.engine.NodeConfigurationEngine;
import com.codeheadsystems.dstore.control.dao.NodeRangeDao;
import com.codeheadsystems.dstore.control.engine.NodeAvailabilityEngine;
import com.codeheadsystems.dstore.control.model.ImmutableNodeRange;
import com.codeheadsystems.dstore.control.model.NodeRange;
import com.codeheadsystems.dstore.node.api.TenantTableVersion;
import com.codeheadsystems.metrics.Metrics;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages node ranges.
 */
@Singleton
public class NodeRangeManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeRangeManager.class);

  private final NodeRangeDao nodeRangeDao;
  private final Clock clock;
  private final Metrics metrics;
  private final NodeAvailabilityEngine nodeAvailabilityEngine;
  private final NodeConfigurationEngine nodeConfigurationEngine;

  /**
   * Constructor.
   *
   * @param nodeRangeDao            the node range dao.
   * @param clock                   for creation.
   * @param metrics                 for timing.
   * @param nodeAvailabilityEngine  for finding nodes.
   * @param nodeConfigurationEngine for updating the configuration engine.
   */
  @Inject
  public NodeRangeManager(final NodeRangeDao nodeRangeDao,
                          final Clock clock,
                          final Metrics metrics,
                          final NodeAvailabilityEngine nodeAvailabilityEngine,
                          final NodeConfigurationEngine nodeConfigurationEngine) {
    LOGGER.info("NodeRangeManager({},{},{},{},{})",
        nodeRangeDao, clock, metrics, nodeAvailabilityEngine, nodeConfigurationEngine);
    this.clock = clock;
    this.metrics = metrics;
    this.nodeRangeDao = nodeRangeDao;
    this.nodeAvailabilityEngine = nodeAvailabilityEngine;
    this.nodeConfigurationEngine = nodeConfigurationEngine;
  }

  /**
   * List the resources for the tenant.
   *
   * @param tenant to query.
   * @return their resources.
   */
  public List<String> resources(final String tenant) {
    LOGGER.trace("resources({})", tenant);
    return metrics.time("NodeRangeManager.resources",
        () -> nodeRangeDao.resources(tenant));
  }

  /**
   * Return back created node ranges that are being used.
   *
   * @param tenant   the tenant.
   * @param resource the resource.
   * @return the list.
   */
  public List<NodeRange> createTenantResource(final String tenant,
                                              final String resource) {
    LOGGER.trace("createTenantResource({},{})", tenant, resource);
    return metrics.time("NodeRangeManager.resources", () -> {
      final List<NodeRange> currentList = nodeRangeDao.nodeRanges(tenant, resource);
      if (currentList.size() > 0) {
        LOGGER.trace("Already exists, returning what we have");
        return currentList; // TODO: Validate the config engine has these entries.
      }
      // TODO: The following needs to be smarter about getting nodes. This is just to set it up.
      final List<NodeRange> nodeRange = nodeAvailabilityEngine.getAvailableNodes(1)
          .stream().map(uuid -> ImmutableNodeRange.builder()
              .uuid(uuid).tenant(tenant).resource(resource).tableVersion(TenantTableVersion.V1SingleEntry)
              .createDate(clock.instant()).status("INIT").ready(false)
              .lowHash(Integer.MIN_VALUE).highHash(Integer.MAX_VALUE)
              .build())
          .collect(Collectors.toList());
      nodeRange.forEach(nodeRangeDao::insert); // TODO: This should be done in a transaction. All or nothing.
      final TenantResource tenantResource = ImmutableTenantResource.builder().tenant(tenant).resource(resource).build();
      nodeRange.stream().map(nr -> ImmutableNodeTenantResourceRange.builder()
          .nodeTenantResource(
              ImmutableNodeTenantResource.builder().uuid(nr.uuid()).tenantResource(tenantResource).build())
          .range(ImmutableRange.builder().lowHash(nr.lowHash()).highHash(nr.highHash()).build())
          .build())
          .forEach(nodeConfigurationEngine::write); // TODO: This should be done in a transaction. All or nothing.
      return nodeRange;
    });
  }

}
