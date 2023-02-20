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

/**
 * Manages the nodes in the swarm.
 */
@Singleton
public class NodeManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);

  private final NodeDao nodeDao;
  private final KeyManager keyManager;
  private final NodeVerificationManager nodeVerificationManager;
  private final Clock clock;
  private final Metrics metrics;

  /**
   * Constructor.
   *
   * @param nodeDao                 for node mgmt.
   * @param keyManager              for key mgmt.
   * @param nodeVerificationManager for verification.
   * @param clock                   the clock.
   * @param metrics                 for metrics.
   */
  @Inject
  public NodeManager(final NodeDao nodeDao,
                     final KeyManager keyManager,
                     final NodeVerificationManager nodeVerificationManager,
                     final Clock clock, final Metrics metrics) {
    this.nodeVerificationManager = nodeVerificationManager;
    this.clock = clock;
    this.metrics = metrics;
    LOGGER.info("NodeManager({},{},{})", nodeDao, keyManager, nodeVerificationManager);
    this.nodeDao = nodeDao;
    this.keyManager = keyManager;
  }

  /**
   * Creates the node. Errors if they already exist.
   *
   * @param uuid     of the node.
   * @param metaData of the node.
   * @return status of the node.
   */
  public Node create(final String uuid, final NodeMetaData metaData) {
    LOGGER.trace("create({}{})", uuid, metaData);
    return metrics.time("NodeManager.create", () -> {
      final Optional<String> currentStatus = status(uuid);
      if (currentStatus.isPresent()) {
        throw new IllegalArgumentException("Node already exists:" + uuid);
      }
      final Node node = ImmutableNode.builder()
          .uuid(uuid)
          .host(metaData.host())
          .port(metaData.port())
          .verified(nodeVerificationManager.verify(uuid, metaData))
          .createDate(clock.instant())
          .status(NodeInfo.Status.DISABLED.name())
          .build();
      nodeDao.insert(node);
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
        .orElseThrow(() -> new IllegalArgumentException("Node not found or not enabled:" + uuid)));
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
        .orElseThrow(() -> new IllegalArgumentException("Node not found or not enabled:" + uuid)));
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
        LOGGER.warn("Node not found: {}", uuid);
        throw new IllegalArgumentException("No such node");
      } else if (NodeInfo.Status.BANNED.name().equals(currentNode.status())) {
        LOGGER.warn("Baned node: {}", uuid);
        throw new IllegalArgumentException("Banned node:" + uuid);
      } else if (!NodeInfo.Status.ENABLED.name().equals(currentNode.status())) {
        LOGGER.trace("Enabling: {}", uuid);
        final Node newNode = ImmutableNode.copyOf(currentNode).withStatus(NodeInfo.Status.ENABLED.name())
            .withUpdateDate(clock.instant());
        nodeDao.update(newNode);
        return newNode;
      } else {
        LOGGER.trace("Already enabled: {}", uuid);
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
    LOGGER.trace("enable({})", uuid);
    return metrics.time("NodeManager.disable", () -> {
      final Node currentNode = nodeDao.read(uuid);
      if (currentNode == null) {
        LOGGER.warn("Node not found: {}", uuid);
        throw new IllegalArgumentException("No such node");
      } else if (NodeInfo.Status.BANNED.name().equals(currentNode.status())) {
        LOGGER.warn("Baned node: {}", uuid);
        throw new IllegalArgumentException("Banned node:" + uuid);
      } else if (!NodeInfo.Status.DISABLED.name().equals(currentNode.status())) {
        LOGGER.trace("Disabling: {}", uuid);
        final Node newNode = ImmutableNode.copyOf(currentNode).withStatus(NodeInfo.Status.DISABLED.name())
            .withUpdateDate(clock.instant());
        nodeDao.update(newNode);
        return newNode;
      } else {
        LOGGER.trace("Already disabled: {}", uuid);
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
        LOGGER.trace("Not found: {}", uuid);
        return Optional.empty();
      } else if (NodeInfo.Status.BANNED.name().equals(node.status())) {
        LOGGER.warn("Banned: {}", uuid);
        return Optional.empty();
      } else {
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
    LOGGER.trace("status({}): ", uuid);
    return metrics.time("NodeManager.status", () -> {
      final Node node = nodeDao.read(uuid);
      if (node == null) {
        LOGGER.trace("status({}): null", uuid);
        return Optional.empty();
      } else {
        LOGGER.trace("status({}): {}}", uuid, node.status());
        return Optional.of(node.status());
      }
    });
  }

}
