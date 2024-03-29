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

package org.svarm.node.accessor;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.metrics.Metrics;
import feign.FeignException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.control.common.api.ControlNodeService;
import org.svarm.control.common.api.ImmutableNodeMetaData;
import org.svarm.control.common.api.NodeInfo;
import org.svarm.control.common.api.NodeMetaData;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * Provides access to the control plane.
 */
@Singleton
public class ControlAccessor {

  private static final Logger LOGGER = getLogger(ControlAccessor.class);

  private final ControlNodeService controlNodeService;
  private final Metrics metrics;

  /**
   * Constructor.
   *
   * @param controlNodeService for accessing the service.
   * @param metrics            for metrics.
   */
  @Inject
  public ControlAccessor(final ControlNodeService controlNodeService,
                         final Metrics metrics) {
    this.controlNodeService = controlNodeService;
    this.metrics = metrics;
    LOGGER.info("ControlAccessor({},{})", controlNodeService, metrics);
  }

  /**
   * Provides the status for the node. If it's a 404 exception, just return empty.
   *
   * @param uuid to check.
   * @return the optional status.
   */
  public Optional<String> status(final String uuid) {
    LOGGER.trace("status({})", uuid);
    return metrics.time("ControlAccessor.status", () -> {
      try {
        Optional<String> result = Optional.ofNullable(controlNodeService.status(uuid))
            .map(NodeInfo::status);
        LOGGER.trace("status({}) {}", uuid, result);
        return result;
      } catch (FeignException.NotFound e) {
        LOGGER.info("status({}) Node not found", uuid); // not an error, but we need to tell this.
        return Optional.empty();
      }
    });
  }

  /**
   * Enables the node.
   *
   * @param uuid to enable.
   */
  public void enable(final String uuid) {
    LOGGER.trace("enable({})", uuid);
    metrics.time("ControlAccessor.enable", () -> {
      final NodeInfo info = controlNodeService.enable(uuid);
      LOGGER.trace("enable result:{}", info);
      return null;
    });
  }

  /**
   * Enables the tenant table.
   *
   * @param uuid       from us.
   * @param identifier the table.
   */
  public void enable(final String uuid,
                     final TenantTableIdentifier identifier) {
    LOGGER.trace("enable({},{})", uuid, identifier);
    metrics.time("ControlAccessor.enable", () -> {
      final NodeInfo info = controlNodeService.enable(uuid, identifier.tenantId(), identifier.tableName());
      LOGGER.info("enable result:{},{}", identifier, info);
      return null;
    });
  }

  /**
   * Tells the control plane the table is deleted.
   *
   * @param nodeUuid   from us.
   * @param identifier the table.
   */
  public void delete(final String nodeUuid,
                     final TenantTableIdentifier identifier) {
    LOGGER.trace("delete({},{})", nodeUuid, identifier);
    metrics.time("ControlAccessor.delete", () -> {
      final NodeInfo info = controlNodeService.delete(nodeUuid, identifier.tenantId(), identifier.tableName());
      LOGGER.trace("result:{}", info);
      return null;
    });
  }

  /**
   * Disable the node.
   *
   * @param uuid to enable.
   */
  public void disable(final String uuid) {
    LOGGER.trace("disable({})", uuid);
    metrics.time("ControlAccessor.disable", () -> {
      final NodeInfo info = controlNodeService.disable(uuid);
      LOGGER.trace("result:{}", info);
      return null;
    });
  }

  /**
   * Registers this node.
   *
   * @param uuid to register.
   * @param uri  the uri.
   */
  public void register(final String uuid, final String uri) {
    LOGGER.trace("register({},{})", uuid, uri);
    metrics.time("ControlAccessor.register", () -> {
      final NodeMetaData metaData = ImmutableNodeMetaData.builder().uri(uri).build();
      final NodeInfo info = controlNodeService.register(uuid, metaData);
      LOGGER.info("register result:{}", info);
      return null;
    });
  }

  /**
   * Returns the key for the node.
   *
   * @param uuid of the node.
   * @return the key.
   */
  public String keyForNode(final String uuid) {
    LOGGER.trace("keyForNode({})", uuid);
    return metrics.time("ControlAccessor.keyForNode",
        () -> controlNodeService.nodeKey(uuid).key());
  }

  /**
   * Returns the key for the resource related to the node.
   *
   * @param uuid       of the node.
   * @param resourceId the resource.
   * @return the key.
   */
  public String keyForResource(final String uuid, final String resourceId) {
    LOGGER.trace("keyForResource({},{})", uuid, resourceId);
    return metrics.time("ControlAccessor.keyForNode",
        () -> controlNodeService.nodeKey(uuid, resourceId).key());
  }
}
