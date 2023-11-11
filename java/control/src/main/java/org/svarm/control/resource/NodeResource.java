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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.control.common.api.ControlNodeService;
import org.svarm.control.common.api.KeyInfo;
import org.svarm.control.common.api.NodeInfo;
import org.svarm.control.common.api.NodeMetaData;
import org.svarm.control.converter.KeyInfoConverter;
import org.svarm.control.converter.NodeInfoConverter;
import org.svarm.control.manager.NodeManager;
import org.svarm.control.manager.NodeRangeManager;
import org.svarm.control.model.Node;
import org.svarm.server.exception.NotFoundException;
import org.svarm.server.resource.JerseyResource;

/**
 * The resource for nodes.
 */
@Singleton
public class NodeResource implements JerseyResource, ControlNodeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeResource.class);

  private final NodeManager nodeManager;
  private final NodeRangeManager nodeRangeManager;
  private final NodeInfoConverter nodeInfoConverter;
  private final KeyInfoConverter keyInfoConverter;

  /**
   * The constructor.
   *
   * @param nodeManager       to use.
   * @param nodeRangeManager  for node ranges.
   * @param nodeInfoConverter converter.
   * @param keyInfoConverter  converter.
   */
  @Inject
  public NodeResource(final NodeManager nodeManager,
                      final NodeRangeManager nodeRangeManager,
                      final NodeInfoConverter nodeInfoConverter,
                      final KeyInfoConverter keyInfoConverter) {
    this.nodeRangeManager = nodeRangeManager;
    LOGGER.info("NodeResource({},{})", nodeManager, nodeInfoConverter);
    this.nodeManager = nodeManager;
    this.nodeInfoConverter = nodeInfoConverter;
    this.keyInfoConverter = keyInfoConverter;
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public NodeInfo register(final String nodeUuid, final NodeMetaData metaData) {
    LOGGER.trace("register({},{})", nodeUuid, metaData);
    return nodeInfoConverter.from(nodeManager.create(nodeUuid, metaData));
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public NodeInfo enable(final String nodeUuid) {
    LOGGER.trace("enable({})", nodeUuid);
    final Node node = nodeManager.enable(nodeUuid);
    return nodeInfoConverter.from(node);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public NodeInfo enable(final String nodeUuid,
                         final String tenant,
                         final String resource) {
    LOGGER.trace("enable({},{},{})", nodeUuid, tenant, resource);
    final Node node = nodeManager.read(nodeUuid)
        .orElseThrow(() -> new NotFoundException("No such node"));
    nodeRangeManager.setReady(nodeUuid, tenant, resource, true);
    return nodeInfoConverter.from(node);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public NodeInfo disable(final String nodeUuid) {
    LOGGER.trace("disable({})", nodeUuid);
    final Node node = nodeManager.disable(nodeUuid);
    return nodeInfoConverter.from(node);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public NodeInfo disable(final String nodeUuid,
                          final String tenant,
                          final String resource) {
    LOGGER.trace("disable({},{},{})", nodeUuid, tenant, resource);
    final Node node = nodeManager.read(nodeUuid)
        .orElseThrow(() -> new NotFoundException("No such node"));
    nodeRangeManager.setReady(nodeUuid, tenant, resource, false);
    return nodeInfoConverter.from(node);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public NodeInfo delete(final String nodeUuid,
                         final String tenant,
                         final String resource) {
    LOGGER.trace("delete({},{},{})", nodeUuid, tenant, resource);
    final Node node = nodeManager.read(nodeUuid)
        .orElseThrow(() -> new NotFoundException("No such node"));
    nodeRangeManager.finalizeDelete(nodeUuid, tenant, resource);
    return nodeInfoConverter.from(node);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public NodeInfo status(final String nodeUuid) {
    LOGGER.trace("status({})", nodeUuid);
    final Node node = nodeManager.read(nodeUuid)
        .orElseThrow(() -> new NotFoundException("No such uuid: " + nodeUuid));
    return nodeInfoConverter.from(node);
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public KeyInfo nodeKey(final String nodeUuid) {
    LOGGER.trace("nodeKey({})", nodeUuid);
    return keyInfoConverter.from(nodeManager.key(nodeUuid));
  }

  @Override
  @Timed
  @ExceptionMetered
  @ResponseMetered
  public KeyInfo nodeKey(final String nodeUuid, final String reference) {
    LOGGER.trace("nodeKey({},{})", nodeUuid, reference);
    return keyInfoConverter.from(nodeManager.key(nodeUuid, reference));
  }
}
