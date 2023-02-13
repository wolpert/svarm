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
import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import com.codeheadsystems.dstore.control.common.api.KeyInfo;
import com.codeheadsystems.dstore.control.common.api.NodeInfo;
import com.codeheadsystems.dstore.control.common.api.NodeMetaData;
import com.codeheadsystems.dstore.control.converter.KeyInfoConverter;
import com.codeheadsystems.dstore.control.converter.NodeInfoConverter;
import com.codeheadsystems.dstore.control.manager.NodeManager;
import com.codeheadsystems.dstore.control.manager.NodeRangeManager;
import com.codeheadsystems.dstore.control.model.Node;
import com.codeheadsystems.dstore.control.model.NodeRange;
import com.codeheadsystems.server.exception.NotFoundException;
import com.codeheadsystems.server.resource.JerseyResource;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    final NodeRange nodeRange = nodeRangeManager.getNodeRange(nodeUuid, tenant, resource)
        .orElseThrow(() -> new NotFoundException("No resource for node"));
    nodeRangeManager.setReady(nodeRange, true);
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
    final NodeRange nodeRange = nodeRangeManager.getNodeRange(nodeUuid, tenant, resource)
        .orElseThrow(() -> new NotFoundException("No resource for node"));
    nodeRangeManager.setReady(nodeRange, false);
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
