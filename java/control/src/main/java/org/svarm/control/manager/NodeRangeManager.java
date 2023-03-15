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

package org.svarm.control.manager;

import com.codeheadsystems.metrics.Metrics;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.config.api.ImmutableMetaData;
import org.svarm.common.config.api.ImmutableNodeTenantResourceRange;
import org.svarm.common.config.api.ImmutableTenantResource;
import org.svarm.common.config.api.ImmutableTenantResourceRange;
import org.svarm.common.config.api.MetaData;
import org.svarm.common.config.api.NodeTenantResourceRange;
import org.svarm.common.config.api.TenantResourceRange;
import org.svarm.common.config.engine.NodeConfigurationEngine;
import org.svarm.control.converter.NodeRangeConverter;
import org.svarm.control.dao.NodeRangeDao;
import org.svarm.control.engine.NodeAvailabilityEngine;
import org.svarm.control.engine.RingHashSplitEngine;
import org.svarm.control.model.ImmutableNodeRange;
import org.svarm.control.model.NodeRange;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.server.exception.NotFoundException;

/**
 * Manages node ranges.
 */
@Singleton
public class NodeRangeManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeRangeManager.class);
  private static final int DEFAULT_CLUSTER_SIZE = 2;

  private final NodeRangeDao nodeRangeDao;
  private final Clock clock;
  private final Metrics metrics;
  private final NodeAvailabilityEngine nodeAvailabilityEngine;
  private final NodeConfigurationEngine nodeConfigurationEngine;
  private final RingHashSplitEngine ringHashSplitEngine;
  private final NodeRangeConverter nodeRangeConverter;

  /**
   * Constructor.
   *
   * @param nodeRangeDao            the node range dao.
   * @param clock                   for creation.
   * @param metrics                 for timing.
   * @param nodeAvailabilityEngine  for finding nodes.
   * @param nodeConfigurationEngine for updating the configuration engine.
   * @param ringHashSplitEngine     for getting hash values.
   * @param nodeRangeConverter      for conversion.
   */
  @Inject
  public NodeRangeManager(final NodeRangeDao nodeRangeDao,
                          final Clock clock,
                          final Metrics metrics,
                          final NodeAvailabilityEngine nodeAvailabilityEngine,
                          final NodeConfigurationEngine nodeConfigurationEngine,
                          final RingHashSplitEngine ringHashSplitEngine,
                          final NodeRangeConverter nodeRangeConverter) {
    this.clock = clock;
    this.metrics = metrics;
    this.nodeRangeDao = nodeRangeDao;
    this.nodeAvailabilityEngine = nodeAvailabilityEngine;
    this.nodeConfigurationEngine = nodeConfigurationEngine;
    this.ringHashSplitEngine = ringHashSplitEngine;
    this.nodeRangeConverter = nodeRangeConverter;
    LOGGER.info("NodeRangeManager({},{},{},{},{})",
        nodeRangeDao, clock, metrics, nodeAvailabilityEngine, nodeConfigurationEngine);
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
   * Sets the ready boolean for the node that is part of the cluster. If all nodes are ready, will make the cluster
   * ready.
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
      if (nodeRange.ready().equals(ready)) {
        LOGGER.trace("Ready already set: {}:{}", ready, nodeRange);
        return nodeRange;
      }
      final NodeRange updated = ImmutableNodeRange.copyOf(nodeRange).withReady(ready);
      nodeRangeDao.update(updated);
      final boolean allReady = getNodeRange(tenant, resource).stream().allMatch(NodeRange::ready);
      if (allReady) {
        updateTenantResourceConfiguration(tenant, resource);
      }
      return updated;
    });
  }

  /**
   * Finalize the delete of a tenant resource from the cluster.
   *
   * @param nodeUuid that has the deletion.
   * @param tenant   the owns the resource.
   * @param resource to delete from the nodeuuid cluster.
   */
  public void finalizeDelete(final String nodeUuid,
                             final String tenant,
                             final String resource) {
    LOGGER.trace("finalizeDelete({},{},{})", nodeUuid, tenant, resource);
    getNodeRange(nodeUuid, tenant, resource)
        .ifPresentOrElse(nodeRange -> {
          if (nodeRange.status().equals(NodeRange.STATUS_DELETING)) {
            LOGGER.info("Deleting: {}", nodeRange);
            nodeRangeDao.delete(nodeUuid, tenant, resource);
            nodeConfigurationEngine.deleteNodeTenantResourceRange(nodeUuid, tenant, resource);
          } else {
            LOGGER.warn("Not in deleting state, ignoring! {}", nodeRange);
          }
        }, () -> nodeConfigurationEngine.deleteNodeTenantResourceRange(nodeUuid, tenant, resource)); // healing
  }

  /**
   * Get the node range for the tenant resource on a specific node.
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
   * Gets the node range list, if it exists. This will include all node in a tenant resource.
   * As seen by the proxies.
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
   * Updates the configuration service (like etcd) with the list of node ranges from the tenant resource namespace.
   * Used by the proxy.
   *
   * @param tenant   to get.
   * @param resource to get.
   */
  public void updateTenantResourceConfiguration(final String tenant,
                                                final String resource) {
    LOGGER.trace("updateConfiguration({},{})", tenant, resource);
    metrics.time("NodeRangeManager.updateConfiguration", () -> {
      final List<org.svarm.common.config.api.NodeRange> nodeRanges =
          nodeRangeDao.apiNodeRanges(tenant, resource);
      final Map<Integer, org.svarm.common.config.api.NodeRange> map = nodeRanges.stream()
          .collect(Collectors.toMap(org.svarm.common.config.api.NodeRange::hash, nr -> nr));
      final TenantResourceRange tenantResourceRange = ImmutableTenantResourceRange.builder()
          .tenant(tenant).resource(resource).hashToNodeRange(map).build();
      nodeConfigurationEngine.write(tenantResourceRange);
      return null;
    });
  }

  /**
   * Return back created node ranges that are being used. This creates the database entries and the etcd entries
   * on the node side. It will not update the tenant/proxy side, meaning the proxies won't see them yet.
   * If they already exist, it will ensure the etcd is up-to-date.
   *
   * @param tenant          the tenant.
   * @param resource        the resource.
   * @param tableDefinition the table definition.
   * @return the list.
   */
  public List<NodeRange> createTenantResource(final String tenant,
                                              final String resource,
                                              final TableDefinition tableDefinition) {
    LOGGER.info("createTenantResource({},{})", tenant, resource);
    return metrics.time("NodeRangeManager.resources", () -> {
      final List<NodeRange> nodeRange = getOrCreateNodeRangeList(tenant, resource, tableDefinition);
      final List<NodeTenantResourceRange> nodeTenantResourceRanges = nodeRangeConverter
          .toNodeTenantResourceRanges(tenant, resource, nodeRange);
      nodeConfigurationEngine.write(nodeTenantResourceRanges);
      LOGGER.info("Create for now resource, results: {},{},{}", tenant, resource, nodeRange);
      return nodeRange;
    });
  }

  private List<NodeRange> getOrCreateNodeRangeList(final String tenant,
                                                   final String resource,
                                                   final TableDefinition tableDefinition) {
    final List<NodeRange> currentList = nodeRangeDao.nodeRanges(tenant, resource);
    if (currentList.size() > 0) {
      LOGGER.info("Create called on existing resource, using what we have: {},{},{}", tenant, resource, currentList);
      return currentList;
    }
    final List<Integer> hashes = ringHashSplitEngine.evenSplitHashes(DEFAULT_CLUSTER_SIZE);
    final List<NodeRange> nodeRange = nodeAvailabilityEngine.getAvailableNodes(DEFAULT_CLUSTER_SIZE)
        .stream().map(nodeUuid -> ImmutableNodeRange.builder()
            .nodeUuid(nodeUuid).tenant(tenant).resource(resource).tableVersion(tableDefinition.name())
            .createDate(clock.instant()).status(NodeRange.STATUS_INIT).ready(false)
            .hash(hashes.remove(0))
            .build())
        .collect(Collectors.toList());
    nodeRangeDao.useTransaction(t -> nodeRange.forEach(t::insert));
    return nodeRange;
  }

  /**
   * Deletes the tenant resource.
   *
   * @param tenantId who owns the resource.
   * @param resource the resource to delete.
   */
  public void deleteTenantResource(final String tenantId,
                                   final String resource) {
    final List<NodeRange> nodeRange = nodeRangeDao.nodeRanges(tenantId, resource);
    nodeRangeDao.useTransaction(t ->
        nodeRange.forEach(nr ->
            nodeRangeDao.update(ImmutableNodeRange.copyOf(nr).withStatus(NodeRange.STATUS_DELETING))));
    final List<NodeTenantResourceRange> nodeTenantResourceRanges = nodeRangeConverter
        .toNodeTenantResourceRanges(tenantId, resource, nodeRange).stream().map(range -> {
          final MetaData updatedMetaData = ImmutableMetaData.copyOf(range.metaData()).withAction(MetaData.ACTION_DELETE);
          return ImmutableNodeTenantResourceRange.copyOf(range).withMetaData(updatedMetaData);
        }).collect(Collectors.toList());
    nodeConfigurationEngine.write(nodeTenantResourceRanges);
    nodeConfigurationEngine
        .readTenantResourceRange(ImmutableTenantResource.builder().tenant(tenantId).resource(resource).build())
        .ifPresent(nodeConfigurationEngine::delete);
  }
}
