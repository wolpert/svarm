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

package org.svarm.common.config.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.config.accessor.EtcdAccessor;
import org.svarm.common.config.api.ImmutableTenantResourceRange;
import org.svarm.common.config.api.NodeRange;
import org.svarm.common.config.api.NodeTenantResource;
import org.svarm.common.config.api.NodeTenantResourceRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.api.TenantResourceRange;
import org.svarm.common.config.converter.NodeTenantResourceRangeConverter;
import org.svarm.common.engine.JsonEngine;

/**
 * Provides the features needed to store node data into the configuration service, like etcd or zookeeper.
 * Node view, key = node/{uuid}/id/{tenant}/{tenantResource}
 * value={"lowHash":0,"highHash":32767}
 * Tenant view, key= tenant/{tenant}/{tenantResource}/{lowHash}
 * value = {"node":"{uuid}", "highHash":32767, "uri":"{uri}"}
 */
@Singleton
public class NodeConfigurationEngine {

  /**
   * The tenant namespace.
   */
  public static final String TENANT_NAMESPACE = "tenant";
  private static final Logger LOGGER = LoggerFactory.getLogger(NodeConfigurationEngine.class);
  private static final String NODE_NAMESPACE = "node";
  private static final TypeReference<Map<Integer, NodeRange>> MAP_TYPE_REFERENCE = new TypeReference<>() {
  };
  private final EtcdAccessor accessor;
  private final JsonEngine jsonEngine;
  private final NodeTenantResourceRangeConverter nodeTenantResourceRangeConverter;

  /**
   * Constructor.
   *
   * @param accessor                         for reading/writing values.
   * @param jsonEngine                       for updating json.
   * @param nodeTenantResourceRangeConverter converter.
   */
  @Inject
  public NodeConfigurationEngine(final EtcdAccessor accessor,
                                 final JsonEngine jsonEngine,
                                 final NodeTenantResourceRangeConverter nodeTenantResourceRangeConverter) {
    this.nodeTenantResourceRangeConverter = nodeTenantResourceRangeConverter;
    this.jsonEngine = jsonEngine;
    this.accessor = accessor;
    LOGGER.info("NodeConfigurationEngine({},{},{})", accessor, jsonEngine, nodeTenantResourceRangeConverter);
  }

  /**
   * Writes to configuration store the nodes tenant resource.
   *
   * @param resourceRange resource range.
   */
  public void write(final NodeTenantResourceRange resourceRange) {
    LOGGER.trace("write({})", resourceRange);
    final NodeTenantResource nodeTenantResource = resourceRange.nodeTenantResource();
    final TenantResource tenantResource = nodeTenantResource.tenantResource();
    final String key = String.format("%s/id/%s/%s",
        nodeTenantResource.uuid(), tenantResource.tenant(), tenantResource.resource());
    final String value = jsonEngine.writeValue(resourceRange.metaData());
    LOGGER.trace("put {} {}", key, value);
    accessor.put(NODE_NAMESPACE, key, value);
  }

  /**
   * Writes to configuration store the nodes tenant resource.
   *
   * @param resourceRanges resource ranges.
   */
  public void write(final List<NodeTenantResourceRange> resourceRanges) {
    LOGGER.trace("write({})", resourceRanges);
    final Map<String, String> resourceMap = resourceRanges.stream().collect(Collectors.toMap(resourceRange -> {
      final NodeTenantResource nodeTenantResource = resourceRange.nodeTenantResource();
      final TenantResource tenantResource = nodeTenantResource.tenantResource();
      return String.format("%s/id/%s/%s", nodeTenantResource.uuid(), tenantResource.tenant(), tenantResource.resource());
    }, resourceRange -> jsonEngine.writeValue(resourceRange.metaData())));
    accessor.putAll(NODE_NAMESPACE, resourceMap);
  }

  /**
   * Writes to configuration store the nodes tenant resource.
   *
   * @param resourceRange resource range.
   */
  public void write(final TenantResourceRange resourceRange) {
    LOGGER.trace("write({})", resourceRange);
    final String key = String.format("%s/%s", resourceRange.tenant(), resourceRange.resource());
    final String value = jsonEngine.writeValue(resourceRange.hashToNodeRange());
    accessor.put(TENANT_NAMESPACE, key, value);
  }

  /**
   * Delete the resource.
   *
   * @param resourceRange resource.
   */
  public void delete(final NodeTenantResourceRange resourceRange) {
    LOGGER.trace("delete({})", resourceRange);
    final NodeTenantResource nodeTenantResource = resourceRange.nodeTenantResource();
    final TenantResource tenantResource = nodeTenantResource.tenantResource();
    deleteNodeTenantResourceRange(nodeTenantResource.uuid(), tenantResource.tenant(), tenantResource.resource());
  }

  /**
   * Delete of the node tenant resource for the node itself.
   *
   * @param uuid     of the node.
   * @param tenant   the tenant.
   * @param resource the resource.
   */
  public void deleteNodeTenantResourceRange(final String uuid,
                                            final String tenant,
                                            final String resource) {
    LOGGER.trace("deleteNodeTenantResourceRange({},{},{})", uuid, tenant, resource);
    final String key = String.format("%s/id/%s/%s", uuid, tenant, resource);
    accessor.delete(NODE_NAMESPACE, key);
  }

  /**
   * Delete the resource.
   *
   * @param resourceRange resource.
   */
  public void delete(final TenantResourceRange resourceRange) {
    LOGGER.trace("delete({})", resourceRange);
    final String key = String.format("%s/%s", resourceRange.tenant(), resourceRange.resource());
    accessor.delete(TENANT_NAMESPACE, key);
  }

  /**
   * Reads all node resources from etcd.
   *
   * @param uuid of the node.
   * @return the list of resource ranges.
   */
  public List<NodeTenantResourceRange> readNodeResources(final String uuid) {
    LOGGER.trace("readNodeResources({})", uuid);
    final String key = String.format("%s/id/", uuid);
    final Map<String, String> map = accessor.getAll(NODE_NAMESPACE, key);
    return nodeTenantResourceRangeConverter.from(map);
  }

  /**
   * Reads all the configuration for the tenant resource.
   *
   * @param tenantResource of the node.
   * @return the list of resource ranges.
   */
  public Optional<TenantResourceRange> readTenantResourceRange(final TenantResource tenantResource) {
    LOGGER.trace("readTenantResourceRange({})", tenantResource);
    final String key = getTenantResourceKey(tenantResource);
    return accessor.get(TENANT_NAMESPACE, key)
        .map(json -> ImmutableTenantResourceRange.builder()
            .tenant(tenantResource.tenant())
            .resource(tenantResource.resource())
            .hashToNodeRange(jsonEngine.readValue(json, MAP_TYPE_REFERENCE))
            .build());
  }

  /**
   * Accessor to the tenant resource key.
   *
   * @param tenantResource we want the key from.
   * @return the key.
   */
  public String getTenantResourceKey(final TenantResource tenantResource) {
    return String.format("%s/%s", tenantResource.tenant(), tenantResource.resource());
  }

}
