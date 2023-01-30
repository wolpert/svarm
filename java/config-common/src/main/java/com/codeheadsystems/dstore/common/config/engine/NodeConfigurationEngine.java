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

package com.codeheadsystems.dstore.common.config.engine;

import com.codeheadsystems.dstore.common.config.accessor.EtcdAccessor;
import com.codeheadsystems.dstore.common.config.api.ImmutableNodeTenantResource;
import com.codeheadsystems.dstore.common.config.api.ImmutableNodeTenantResourceRange;
import com.codeheadsystems.dstore.common.config.api.ImmutableTenantResource;
import com.codeheadsystems.dstore.common.config.api.ImmutableTenantResourceRange;
import com.codeheadsystems.dstore.common.config.api.NodeRange;
import com.codeheadsystems.dstore.common.config.api.NodeTenantResource;
import com.codeheadsystems.dstore.common.config.api.NodeTenantResourceRange;
import com.codeheadsystems.dstore.common.config.api.Range;
import com.codeheadsystems.dstore.common.config.api.TenantResource;
import com.codeheadsystems.dstore.common.config.api.TenantResourceRange;
import com.codeheadsystems.dstore.common.engine.JsonEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the features needed to store node data into the configuration service, like etcd or zookeeper.
 * Node view, key = node/{uuid}/id/{tenant}/{tenantResource}
 * value={"lowHash":0,"highHash":32767}
 * Tenant view, key= tenant/{tenant}/{tenantResource}/{lowHash}
 * value = {"node":"{uuid}", "highHash":32767, "uri":"{uri}"}
 */
@Singleton
public class NodeConfigurationEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeConfigurationEngine.class);
  private static final String NODE_NAMESPACE = "node";
  private static final String TENANT_NAMESPACE = "tenant";
  private static final Pattern LOWHASH_KEY_PATTERN = Pattern.compile("[0-9]+$");

  private static final TypeReference<Set<NodeRange>> SET_TYPE_REFERENCE = new TypeReference<Set<NodeRange>>() {
  };
  private final EtcdAccessor accessor;
  private final JsonEngine jsonEngine;

  /**
   * Constructor.
   *
   * @param accessor   for reading/writing values.
   * @param jsonEngine for updating json.
   */
  @Inject
  public NodeConfigurationEngine(final EtcdAccessor accessor,
                                 final JsonEngine jsonEngine) {
    LOGGER.info("NodeConfigurationEngine({},{})", accessor, jsonEngine);
    this.jsonEngine = jsonEngine;
    this.accessor = accessor;
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
    final String value = jsonEngine.writeValue(resourceRange.range());
    LOGGER.trace("put {} {}", key, value);
    accessor.put(NODE_NAMESPACE, key, value);
  }

  /**
   * Writes to configuration store the nodes tenant resource.
   *
   * @param resourceRange resource range.
   */
  public void write(final TenantResourceRange resourceRange) {
    LOGGER.trace("write({})", resourceRange);
    final Map<String, String> map = resourceRange.hashToNodeRangeSet().entrySet().stream()
        .collect(Collectors.toMap(
            e -> String.format("%s/%s/%s", resourceRange.tenant(), resourceRange.resource(), e.getKey()),
            e -> jsonEngine.writeValue(e.getValue())));
    accessor.putAll(TENANT_NAMESPACE, map);
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
    return accessor.getAll(NODE_NAMESPACE, key).entrySet().stream()
        .map(e -> toNodeTenantResourceRange(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }

  /**
   * Converter.
   *
   * @param key   in format: node/{uuid}/id/{tenant}/{tenantResource}
   * @param value in format: {"lowHash":0,"highHash":32767}
   * @return a note tenant resource range.
   */
  private NodeTenantResourceRange toNodeTenantResourceRange(final String key,
                                                            final String value) {
    LOGGER.trace("toNodeTenantResourceRange({},{})", key, value);
    final String[] tokens = key.split("/");
    final Range range = jsonEngine.readValue(value, Range.class);
    final TenantResource tenantResource = ImmutableTenantResource.builder()
        .tenant(tokens[3])
        .resource(tokens[4])
        .build();
    final NodeTenantResource nodeTenantResource = ImmutableNodeTenantResource.builder()
        .tenantResource(tenantResource)
        .uuid(tokens[1])
        .build();
    return ImmutableNodeTenantResourceRange.builder()
        .range(range)
        .nodeTenantResource(nodeTenantResource)
        .build();
  }

  /**
   * Reads all the configuration for the tenent resource.
   *
   * @param tenantResource of the node.
   * @return the list of resource ranges.
   */
  public Optional<TenantResourceRange> readTenantResourceRange(final TenantResource tenantResource) {
    LOGGER.trace("readTenantResourceRange({})", tenantResource);
    final String key = String.format("%s/%s/", tenantResource.tenant(), tenantResource.resource());
    final Map<String, String> map = accessor.getAll(TENANT_NAMESPACE, key);
    if (map.isEmpty()) {
      return Optional.empty();
    }
    final Map<Integer, Set<NodeRange>> hashToNodeRangeSet = map.entrySet().stream()
        .collect(Collectors.toMap(
            e -> keyToLowHash(e.getKey()),
            e -> jsonEngine.readValue(e.getValue(), SET_TYPE_REFERENCE)
        ));
    return Optional.of(ImmutableTenantResourceRange.builder()
        .tenant(tenantResource.tenant())
        .resource(tenantResource.resource())
        .hashToNodeRangeSet(hashToNodeRangeSet)
        .build());
  }

  private int keyToLowHash(final String key) {
    LOGGER.trace("keyToLowHash({})", key);
    final Matcher matcher = LOWHASH_KEY_PATTERN.matcher(key);
    if (matcher.find()) {
      final int value = Integer.parseInt(matcher.group());
      LOGGER.trace("lowHash from key-> {}", value);
      return value;
    } else {
      throw new IllegalArgumentException("Invalid key, cannot match lowHash:" + key);
    }
  }
}
