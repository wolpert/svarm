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
import com.codeheadsystems.dstore.common.config.api.ImmutableTenantResourceRange;
import com.codeheadsystems.dstore.common.config.api.TenantResource;
import com.codeheadsystems.dstore.common.config.api.TenantResourceRange;
import com.codeheadsystems.dstore.common.config.engine.NodeConfigurationEngine;
import com.codeheadsystems.dstore.control.dao.NodeRangeDao;
import com.codeheadsystems.dstore.control.engine.NodeAvailabilityEngine;
import com.codeheadsystems.dstore.control.model.ImmutableNodeRange;
import com.codeheadsystems.dstore.control.model.Node;
import com.codeheadsystems.dstore.control.model.NodeRange;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.server.exception.NotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
  private static final String V_1_SINGLE_ENTRY_ENGINE = "V1SingleEntryEngine";

  private final NodeRangeDao nodeRangeDao;
  private final Clock clock;
  private final Metrics metrics;
  private final NodeAvailabilityEngine nodeAvailabilityEngine;
  private final NodeConfigurationEngine nodeConfigurationEngine;
  private final NodeManager nodeManager;

  /**
   * Constructor.
   *
   * @param nodeRangeDao            the node range dao.
   * @param clock                   for creation.
   * @param metrics                 for timing.
   * @param nodeAvailabilityEngine  for finding nodes.
   * @param nodeConfigurationEngine for updating the configuration engine.
   * @param nodeManager             for getting node details.
   */
  @Inject
  public NodeRangeManager(final NodeRangeDao nodeRangeDao,
                          final Clock clock,
                          final Metrics metrics,
                          final NodeAvailabilityEngine nodeAvailabilityEngine,
                          final NodeConfigurationEngine nodeConfigurationEngine,
                          final NodeManager nodeManager) {
    this.nodeManager = nodeManager;
    LOGGER.info("NodeRangeManager({},{},{},{},{},{})",
        nodeRangeDao, clock, metrics, nodeAvailabilityEngine, nodeConfigurationEngine, nodeManager);
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
   * Sets the ready boolean for the node range.
   *
   * @param nodeUuid to set.
   * @param tenant   to set.
   * @param resource to set.
   * @param ready    the flag.
   * @return the updated nodeRange.
   */
  public NodeRange setReady(final String nodeUuid,
                            final String tenant,
                            final String resource,
                            final boolean ready) {
    LOGGER.trace("setReady({},{},{},{})", nodeUuid, tenant, ready, ready);
    return metrics.time("NodeRangeManager.resources", () -> {
      final NodeRange nodeRange = getNodeRange(nodeUuid, tenant, resource)
          .orElseThrow(() -> new NotFoundException("No resource for node"));
      final NodeRange updated = ImmutableNodeRange.copyOf(nodeRange).withReady(ready);
      nodeRangeDao.update(updated);
      final boolean allReady = getNodeRange(tenant, resource).stream().allMatch(NodeRange::ready);
      if (allReady) {
        updateConfiguration(tenant, resource);
      }
      return updated;
    });
  }

  /**
   * Gets the node range dao, if it exists.
   *
   * @param uuid     of the node.
   * @param tenant   to get.
   * @param resource to get.
   * @return the range.
   */
  public Optional<NodeRange> getNodeRange(final String uuid,
                                          final String tenant,
                                          final String resource) {
    LOGGER.trace("getNodeRange({},{},{})", uuid, tenant, resource);
    return metrics.time("NodeRangeManager.getNodeRange",
        () -> Optional.ofNullable(nodeRangeDao.read(uuid, tenant, resource)));
  }

  /**
   * Gets the node range list, if it exists.
   *
   * @param tenant   to get.
   * @param resource to get.
   * @return the range.
   */
  public List<NodeRange> getNodeRange(final String tenant,
                                      final String resource) {
    LOGGER.trace("getNodeRange({},{})", tenant, resource);
    return metrics.time("NodeRangeManager.getNodeRange",
        () -> nodeRangeDao.nodeRanges(tenant, resource));
  }

  /**
   * Updates the configuration service (like etcd) with the list of node ranges.
   *
   * @param tenant   to get.
   * @param resource to get.
   */
  public void updateConfiguration(final String tenant,
                                  final String resource) {
    LOGGER.trace("updateConfiguration({},{})", tenant, resource);
    metrics.time("NodeRangeManager.updateConfiguration", () -> {
      final List<com.codeheadsystems.dstore.common.config.api.NodeRange> nodeRanges =
          nodeRangeDao.apiNodeRanges(tenant, resource);
      final Map<Integer, Set<com.codeheadsystems.dstore.common.config.api.NodeRange>> map = nodeRanges.stream()
          .collect(Collectors.groupingBy(
              com.codeheadsystems.dstore.common.config.api.NodeRange::lowHash,
              Collectors.mapping(nr -> nr, Collectors.toSet())
          ));
      final TenantResourceRange tenantResourceRange = ImmutableTenantResourceRange.builder()
          .tenant(tenant).resource(resource).hashToNodeRangeSet(map).build();
      nodeConfigurationEngine.write(tenantResourceRange);
      return null;
    });
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
          .stream().map(nodeUuid -> ImmutableNodeRange.builder()
              .nodeUuid(nodeUuid).tenant(tenant).resource(resource).tableVersion(V_1_SINGLE_ENTRY_ENGINE)
              .createDate(clock.instant()).status("INIT").ready(false)
              .lowHash(Integer.MIN_VALUE).highHash(Integer.MAX_VALUE)
              .build())
          .collect(Collectors.toList());
      nodeRange.forEach(nodeRangeDao::insert); // TODO: This should be done in a transaction. All or nothing.
      final TenantResource tenantResource = ImmutableTenantResource.builder().tenant(tenant).resource(resource).build();
      nodeRange.stream().map(nr -> ImmutableNodeTenantResourceRange.builder()
              .nodeTenantResource(
                  ImmutableNodeTenantResource.builder().uuid(nr.nodeUuid()).tenantResource(tenantResource).build())
              .range(ImmutableRange.builder().lowHash(nr.lowHash()).highHash(nr.highHash()).build())
              .build())
          .forEach(nodeConfigurationEngine::write); // TODO: This should be done in a transaction. All or nothing.
      return nodeRange;
    });
  }

}
