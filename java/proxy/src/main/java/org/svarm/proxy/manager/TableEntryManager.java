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

package org.svarm.proxy.manager;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.metrics.Metrics;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.api.NodeRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.api.TenantResourceRange;
import org.svarm.common.engine.HashingEngine;
import org.svarm.proxy.engine.CachingTenantResourceRangeEngine;
import org.svarm.proxy.engine.NodeTenantTableEntryServiceEngine;
import org.svarm.server.exception.NotFoundException;

/**
 * Handles the requests to the various nodes for a single entry.
 */
@Singleton
public class TableEntryManager {

  private static final Logger LOGGER = getLogger(TableEntryManager.class);

  private final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine;
  private final CachingTenantResourceRangeEngine cachingTenantResourceRangeEngine;
  private final HashingEngine hashingEngine;
  private final Metrics metrics;


  /**
   * Constructor.
   *
   * @param nodeTenantTableEntryServiceEngine to get the node connections.
   * @param cachingTenantResourceRangeEngine  to get node configuration data.
   * @param hashingEngine                     to hash the data.
   * @param metrics                           for processing.
   */
  @Inject
  public TableEntryManager(final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine,
                           final CachingTenantResourceRangeEngine cachingTenantResourceRangeEngine,
                           final HashingEngine hashingEngine,
                           final Metrics metrics) {
    this.nodeTenantTableEntryServiceEngine = nodeTenantTableEntryServiceEngine;
    this.cachingTenantResourceRangeEngine = cachingTenantResourceRangeEngine;
    this.hashingEngine = hashingEngine;
    this.metrics = metrics;
    LOGGER.info("TableEntryManager()");
  }

  /**
   * Provide a way to get the entry from the data stores.
   *
   * @param tenantResource tenantResource to lookup.
   * @param entry          the actual entry.
   * @return the value.
   */
  public Optional<JsonNode> getTenantTableEntry(final TenantResource tenantResource,
                                                final String entry) {
    LOGGER.trace("getTenantTableEntry({},{})", tenantResource, entry);
    // get the node lists from etcd.
    final Set<NodeRange> nodeRangeSet = nodeRangeSetForEntry(tenantResource, entry);
    // TODO: fan out the reads concurrently... be smarter here.
    // TODO: This is bad below... but just to get us started.
    for (NodeRange nodeRange : nodeRangeSet) {
      final Optional<JsonNode> result =
          nodeTenantTableEntryServiceEngine.get(nodeRange)
              .readTenantTableEntry(
                  tenantResource.tenant(),
                  tenantResource.resource(),
                  entry);
      if (result.isPresent()) {
        return result;
      }
    }
    return Optional.empty();
  }

  /**
   * Writes the value to all the nodes in the set.
   *
   * @param tenantResource to write.
   * @param entry          the entry.
   * @param data           the data.
   */
  public void putTenantTableEntry(final TenantResource tenantResource,
                                  final String entry,
                                  final JsonNode data) {

    LOGGER.trace("putTenantTableEntry({},{},{})", tenantResource, entry, data);
    // get the node lists from etcd.
    final Set<NodeRange> nodeRangeSet = nodeRangeSetForEntry(tenantResource, entry);
    // TODO: fan out the reads concurrently... be smarter here.
    // TODO: This is bad below... but just to get us started.
    for (NodeRange nodeRange : nodeRangeSet) {
      LOGGER.trace("Processing {}", nodeRange);
      nodeTenantTableEntryServiceEngine.get(nodeRange)
          .createTenantTableEntry(
              tenantResource.tenant(),
              tenantResource.resource(),
              entry,
              data);
    }
  }

  /**
   * Writes the value to all the nodes in the set.
   *
   * @param tenantResource to write.
   * @param entry          the entry.
   */
  public void deleteTenantTableEntry(final TenantResource tenantResource,
                                     final String entry) {

    LOGGER.trace("deleteTenantTableEntry({},{})", tenantResource, entry);
    // get the node lists from etcd.
    final Set<NodeRange> nodeRangeSet = nodeRangeSetForEntry(tenantResource, entry);
    // TODO: fan out the reads concurrently... be smarter here.
    // TODO: This is bad below... but just to get us started.
    for (NodeRange nodeRange : nodeRangeSet) {
      nodeTenantTableEntryServiceEngine.get(nodeRange)
          .deleteTenantTableEntry(
              tenantResource.tenant(),
              tenantResource.resource(),
              entry);
    }
  }

  private Set<NodeRange> nodeRangeSetForEntry(final TenantResource tenantResource,
                                              final String entry) {
    return metrics.time("TableEntryManager.nodeRangeSetForEntry", () -> {
      final TenantResourceRange range = cachingTenantResourceRangeEngine.readTenantResourceRange(tenantResource)
          .orElseThrow(() -> new NotFoundException());
      try {
        LOGGER.trace("Found TenantResourceRange: {}", range);
        final int hash = hashingEngine.murmur3(entry);
        LOGGER.trace("hash: {}", hash);
        return range.hashToNodeRangeSet().entrySet().stream()
            .filter(e -> (e.getKey() <= hash)) // remove the values with a low-hash higher than our hash.
            .max(Map.Entry.comparingByKey()) // low to high
            .map(Map.Entry::getValue)
            .orElseThrow(() -> new IllegalStateException("Unable to find correct set!"));
      } catch (IllegalStateException e) {
        metrics.registry()
            .counter("TableEntryManager.nodeRangeSetForEntry.abjectFailure", metrics.getTags())
            .increment();
        LOGGER.error("Lost data due to lack of nodes! tenant={} entry={} range={}", tenantResource, entry, range);
        throw e;
      }
    });
  }

}
