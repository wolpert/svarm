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

package org.svarm.control.engine;

import com.codeheadsystems.metrics.Metrics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.control.dao.NodeDao;
import org.svarm.control.dao.NodeRangeDao;

/**
 * This engine job is to retrieve nodes that are available for work, based on least used process.
 */
@Singleton
public class NodeAvailabilityEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeAvailabilityEngine.class);

  private final NodeDao nodeDao;
  private final NodeRangeDao nodeRangeDao;
  private final Metrics metrics;

  /**
   * Constructor.
   *
   * @param nodeDao      dao.
   * @param nodeRangeDao dao.
   * @param metrics      metrics.
   */
  @Inject
  public NodeAvailabilityEngine(final NodeDao nodeDao, final NodeRangeDao nodeRangeDao, final Metrics metrics) {
    LOGGER.info("NodeAvailabilityEngine({},{},{})", nodeDao, nodeRangeDao, metrics);
    this.nodeDao = nodeDao;
    this.nodeRangeDao = nodeRangeDao;
    this.metrics = metrics;
  }

  /**
   * Provides a number of nodes that are available. Will throw an exception if it cannot get the number wanted.
   *
   * @param count to look for.
   * @return the list.
   */
  public List<String> getAvailableNodes(final int count) {
    LOGGER.trace("getAvailableNodes({})", count);
    if (count < 1) {
      throw new IllegalArgumentException("Count is invalid: " + count);
    }
    return metrics.time("NodeAvailabilityEngine.getAvailableNodes", () -> {
      final List<String> allNodes = new ArrayList<>(nodeDao.allEnabledNodes()); // make sure it's mutable.
      final Random random = new Random();
      if (allNodes.size() < count) {
        throw new IllegalStateException("Not enough nodes. Available: " + allNodes.size());
      }
      while (allNodes.size() > count) {
        allNodes.remove(random.nextInt(allNodes.size()));
      }
      return allNodes;
    });
  }

}
