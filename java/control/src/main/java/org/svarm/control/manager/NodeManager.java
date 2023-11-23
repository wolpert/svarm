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
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.control.common.api.NodeInfo;
import org.svarm.control.common.api.NodeMetaData;
import org.svarm.control.dao.NodeDao;
import org.svarm.control.model.ImmutableNode;
import org.svarm.control.model.Key;
import org.svarm.control.model.Node;
import org.svarm.server.exception.NotFoundException;

/**
 * Manages the nodes in the swarm.
 */
@Singleton
public class NodeManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);

  private final NodeDao nodeDao;
  private final KeyManager keyManager;
  private final Clock clock;
  private final Metrics metrics;

  /**
   * Constructor.
   *
   * @param nodeDao    for node mgmt.
   * @param keyManager for key mgmt.
   * @param clock      the clock.
   * @param metrics    for metrics.
   */
  @Inject
  public NodeManager(final NodeDao nodeDao,
                     final KeyManager keyManager,
                     final Clock clock,
                     final Metrics metrics) {
    this.clock = clock;
    this.metrics = metrics;
    this.nodeDao = nodeDao;
    this.keyManager = keyManager;
    LOGGER.info("NodeManager({},{},{})", nodeDao, keyManager, metrics);
  }

  /**
   * Creates the node. Errors if they already exist.
   *
   * @param uuid     of the node.
   * @param metaData of the node.
   * @return status of the node.
   */
  public Node create(final String uuid, final NodeMetaData metaData) {
    LOGGER.trace("create({},{})", uuid, metaData);
    return metrics.time("NodeManager.create", () -> {
      final Node currentNode = nodeDao.read(uuid);
      if (currentNode != null) {
        LOGGER.debug("create({}): Node already created: {}", uuid, currentNode);
        return currentNode; // idempotent
      }
      final Node node = ImmutableNode.builder()
          .uuid(uuid)
          .host(metaData.host())
          .port(metaData.port())
          .uri(metaData.uri())
          .createDate(clock.instant())
          .status(NodeInfo.Status.DISABLED.name())
          .build();
      nodeDao.insert(node);
      LOGGER.debug("create({}): Node created: {}", uuid, node);
      return node;
    });
  }

  /**
   * Gets the key for the node. The node must be enabled for this to do anything.
   *
   * @param uuid the node.
   * @return the key.
   */
  public Key key(final String uuid) {
    LOGGER.trace("key({})", uuid);
    return metrics.time("NodeMetrics.key.uuid", () -> status(uuid)
        .filter(this::enabled)
        .map(s -> keyManager.getNodeKey(uuid))
        .orElseThrow(() -> new NotFoundException("NodeKey: Node not found or not enabled:" + uuid)));
  }

  /**
   * Gets the key for the node/tenant. The node must be enabled for this to do anything.
   *
   * @param uuid   the node.
   * @param tenant the tenant.
   * @return the key.
   */
  public Key key(final String uuid, final String tenant) {
    LOGGER.trace("key({},{})", uuid, tenant);
    return metrics.time("NodeMetrics.key.tenant", () -> status(uuid)
        .filter(this::enabled)
        .map(s -> keyManager.getNodeKey(uuid, "tenant:" + tenant))
        .orElseThrow(() -> new NotFoundException("NodeTenantKey: Node not found or not enabled:" + uuid)));
  }

  /**
   * Enables the node.
   *
   * @param uuid the node.
   * @return the resulting node.
   */
  public Node enable(final String uuid) {
    LOGGER.trace("enable({})", uuid);
    return metrics.time("NodeManager.enable", () -> {
      final Node currentNode = nodeDao.read(uuid);
      if (currentNode == null) {
        LOGGER.warn("enable({}): Node not found", uuid);
        throw new NotFoundException("No such node");
      } else if (NodeInfo.Status.BANNED.name().equals(currentNode.status())) {
        LOGGER.warn("enable({}): Baned node: {}", uuid, currentNode);
        throw new IllegalArgumentException("Banned node:" + uuid);
      } else if (!NodeInfo.Status.ENABLED.name().equals(currentNode.status())) {
        LOGGER.debug("enable({}): Enabling: {}", uuid, currentNode);
        final Node newNode = ImmutableNode.copyOf(currentNode)
            .withStatus(NodeInfo.Status.ENABLED.name())
            .withUpdateDate(clock.instant());
        nodeDao.update(newNode);
        LOGGER.debug("enable({}): returning: {}", uuid, newNode);
        return newNode;
      } else {
        LOGGER.debug("enable({}): Already enabled: {}", uuid, currentNode);
        return currentNode;
      }
    });
  }

  /**
   * Disables the node.
   *
   * @param uuid the node.
   * @return the resulting node.
   */
  public Node disable(final String uuid) {
    LOGGER.trace("disable({})", uuid);
    return metrics.time("NodeManager.disable", () -> {
      final Node currentNode = nodeDao.read(uuid);
      if (currentNode == null) {
        LOGGER.warn("disable({}): Node not found", uuid);
        throw new NotFoundException("No such node");
      } else if (NodeInfo.Status.BANNED.name().equals(currentNode.status())) {
        LOGGER.warn("disable({}): Baned node: {}", uuid, currentNode);
        throw new IllegalArgumentException("Banned node:" + uuid);
      } else if (!NodeInfo.Status.DISABLED.name().equals(currentNode.status())) {
        LOGGER.debug("disable({}): Disabling: {}", uuid, currentNode);
        final Node newNode = ImmutableNode.copyOf(currentNode).withStatus(NodeInfo.Status.DISABLED.name())
            .withUpdateDate(clock.instant());
        nodeDao.update(newNode);
        LOGGER.debug("disable({}): results: {}", uuid, newNode);
        return newNode;
      } else {
        LOGGER.debug("disable({}): Already disabled: {}", uuid, currentNode);
        return currentNode;
      }
    });
  }

  private boolean enabled(final String status) {
    return NodeInfo.Status.ENABLED.name().equals(status);
  }

  /**
   * Method provides access to the node. Note, if baned will log an error and pretend it doesn't exist.
   *
   * @param uuid uuid to loo for.
   * @return the node if found.
   */
  public Optional<Node> read(final String uuid) {
    LOGGER.trace("read({})", uuid);
    return metrics.time("NodeManager.read", () -> {
      final Node node = nodeDao.read(uuid);
      if (node == null) {
        LOGGER.error("read({}): Not found", uuid);
        return Optional.empty();
      } else if (NodeInfo.Status.BANNED.name().equals(node.status())) {
        LOGGER.warn("read({}): BannedL {}", uuid, node);
        return Optional.empty();
      } else {
        LOGGER.debug("read({}): found: {}", uuid, node);
        return Optional.of(node);
      }
    });
  }

  /**
   * Returns the status of the node. If the node doesn't exist, the optional will be empty.
   * It WILL returned if it's banded.
   *
   * @param uuid of the node.
   * @return the status.
   */
  public Optional<String> status(final String uuid) {
    LOGGER.trace("status({})", uuid);
    return read(uuid).map(Node::status);
  }

}
