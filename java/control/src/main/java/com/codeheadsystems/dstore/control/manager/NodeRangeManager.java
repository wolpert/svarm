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

import com.codeheadsystems.dstore.control.dao.NodeRangeDao;
import com.codeheadsystems.metrics.Metrics;
import java.time.Clock;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages node ranges.
 */
@Singleton
public class NodeRangeManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeRangeManager.class);

  private final NodeRangeDao nodeRangeDao;
  private final NodeManager nodeManager;
  private final Clock clock;
  private final Metrics metrics;

  /**
   * Constructor.
   *
   * @param nodeRangeDao the dao.
   * @param nodeManager  the node manager.
   * @param clock        for creation.
   * @param metrics      for timing.
   */
  @Inject
  public NodeRangeManager(final NodeRangeDao nodeRangeDao,
                          final NodeManager nodeManager,
                          final Clock clock,
                          final Metrics metrics) {
    LOGGER.info("NodeRangeManager({},{},{},{})", nodeRangeDao, nodeManager, clock, metrics);
    this.clock = clock;
    this.metrics = metrics;
    this.nodeRangeDao = nodeRangeDao;
    this.nodeManager = nodeManager;
  }

  /**
   * List the resources for the tenant.
   *
   * @param tenant to query.
   * @return their resources.
   */
  public List<String> resources(final String tenant) {
    LOGGER.trace("resources({})", tenant);
    return metrics.time("NodeRangeManager.resources",
        () -> nodeRangeDao.resources(tenant));
  }

}
