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

import com.codeheadsystems.dstore.control.common.api.NodeInfo;
import com.codeheadsystems.dstore.control.common.api.NodeMetaData;
import com.codeheadsystems.dstore.control.dao.NodeDao;
import com.codeheadsystems.dstore.control.model.ImmutableNode;
import com.codeheadsystems.dstore.control.model.Key;
import com.codeheadsystems.dstore.control.model.Node;
import java.time.Clock;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /**
   * Constructor.
   *
   * @param nodeDao                 for node mgmt.
   * @param keyManager              for key mgmt.
   * @param nodeVerificationManager for verification.
   * @param clock                   the clock.
   */
  @Inject
  public NodeManager(final NodeDao nodeDao,
                     final KeyManager keyManager,
                     final NodeVerificationManager nodeVerificationManager,
                     final Clock clock) {
    this.nodeVerificationManager = nodeVerificationManager;
    this.clock = clock;
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
  public String create(final String uuid, final NodeMetaData metaData) {
    LOGGER.trace("create({}{})", uuid, metaData);
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
    return node.status();
  }

  /**
   * Gets the key for the node. The node must be enabled for this to do anything.
   *
   * @param uuid the node.
   * @return the key.
   */
  public Key key(final String uuid) {
    LOGGER.trace("key({})", uuid);
    return status(uuid)
        .filter(this::enabled)
        .map(s -> keyManager.getNodeKey(uuid))
        .orElseThrow(() -> new IllegalArgumentException("Node not found or not enabled:" + uuid));
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
    return status(uuid)
        .filter(this::enabled)
        .map(s -> keyManager.getNodeKey(uuid, "tenant:" + tenant))
        .orElseThrow(() -> new IllegalArgumentException("Node not found or not enabled:" + uuid));
  }

  /**
   * Enables the node.
   *
   * @param uuid the node.
   */
  public void enable(final String uuid) {
    LOGGER.trace("enable({})", uuid);
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
    } else {
      LOGGER.trace("Already enabled: {}", uuid);
    }
  }

  /**
   * Disables the node.
   *
   * @param uuid the node.
   */
  public void disable(final String uuid) {
    LOGGER.trace("enable({})", uuid);
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
    } else {
      LOGGER.trace("Already disabled: {}", uuid);
    }
  }

  private boolean enabled(final String status) {
    return NodeInfo.Status.ENABLED.name().equals(status);
  }

  /**
   * Returns the status of the node. If the node doesn't exist, the optional will be empty.
   *
   * @param uuid of the node.
   * @return the status.
   */
  public Optional<String> status(final String uuid) {
    LOGGER.trace("status({}): ", uuid);
    final Node node = nodeDao.read(uuid);
    if (node == null) {
      LOGGER.trace("status({}): null", uuid);
      return Optional.empty();
    } else {
      LOGGER.trace("status({}): {}}", uuid, node.status());
      return Optional.of(node.status());
    }
  }

}
