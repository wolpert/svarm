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

package org.svarm.node.manager;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.metrics.Metrics;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.control.common.api.NodeInfo;
import org.svarm.node.NodeConfiguration;
import org.svarm.node.accessor.ControlAccessor;
import org.svarm.node.model.NodeInternalConfiguration;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * Manager for the control plane.
 */
@Singleton
public class RealControlPlaneManager implements ControlPlaneManager {

  private static final Logger LOGGER = getLogger(RealControlPlaneManager.class);

  private final Metrics metrics;
  private final ControlAccessor controlAccessor;
  private final String nodeUuid;
  private final String host;
  private final Integer port;

  /**
   * Constructor.
   *
   * @param metrics               for metrics.
   * @param controlAccessor       to access the control plane.
   * @param internalConfiguration for configuration.
   * @param nodeConfiguration     for configuration.
   */
  @Inject
  public RealControlPlaneManager(final Metrics metrics,
                                 final ControlAccessor controlAccessor,
                                 final NodeInternalConfiguration internalConfiguration,
                                 final NodeConfiguration nodeConfiguration) {
    this.metrics = metrics;
    this.controlAccessor = controlAccessor;
    this.nodeUuid = internalConfiguration.uuid();
    if (nodeConfiguration.getNodeHost() == null) {
      this.host = getHostByInet();
      LOGGER.trace("Using discovered host: {}", host);
    } else {
      this.host = nodeConfiguration.getNodeHost();
      LOGGER.trace("Using host from configuration: {}", host);
    }
    this.port = nodeConfiguration.getNodePort();
    LOGGER.info("RealControlPlaneManager({},{},{},{})", nodeUuid, controlAccessor, this.host, port);
  }

  private static String getHostnameByServer() {
    return System.getenv("HOSTNAME");
  }

  private static String getHostByInet() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String keyForNode() {
    LOGGER.trace("keyForNode()");
    return metrics.time("RealControlPlaneManager.keyForNode", () -> {
      final String status = getStatus();
      if (!NodeInfo.Status.ENABLED.equals(status)) {
        controlAccessor.enable(nodeUuid);
      }
      return controlAccessor.keyForNode(nodeUuid);
    });
  }

  @Override
  public String keyForTenant(final String tenantId) {
    LOGGER.trace("keyForTenant({})", tenantId);
    return metrics.time("RealControlPlaneManager.keyForTenant",
        () -> controlAccessor.keyForResource(nodeUuid, tenantId));
  }

  @Override
  public void enable(final TenantTableIdentifier identifier) {
    LOGGER.trace("enable({})", identifier);
    metrics.time("RealControlPlaneManager.enableTableTenant",
        () -> {
          controlAccessor.enable(nodeUuid, identifier);
          return null;
        });
  }

  private String getStatus() {
    LOGGER.trace("getStatus()");
    final Optional<String> status = controlAccessor.status(nodeUuid);
    if (status.isPresent()) {
      return status.get();
    }
    controlAccessor.register(nodeUuid, host, port);
    return controlAccessor.status(nodeUuid).orElseThrow(() -> new IllegalStateException("Unable to register"));
  }
}
