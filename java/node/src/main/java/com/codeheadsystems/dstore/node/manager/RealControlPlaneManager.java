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

package com.codeheadsystems.dstore.node.manager;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.control.common.api.NodeInfo;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.accessor.ControlAccessor;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.codeheadsystems.metrics.Metrics;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

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
    this.host = nodeConfiguration.getNodeHost();
    this.port = nodeConfiguration.getNodePort();
    LOGGER.info("RealControlPlaneManager({}.{},{},{})", nodeUuid, controlAccessor, host, port);
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
