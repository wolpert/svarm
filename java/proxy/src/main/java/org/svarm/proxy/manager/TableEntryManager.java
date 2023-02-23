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
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.api.NodeRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.api.TenantResourceRange;
import org.svarm.common.engine.RingEngine;
import org.svarm.common.model.RingEntry;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.api.ImmutableEntryInfo;
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
  private final RingEngine ringEngine;
  private final Clock clock;
  private final Metrics metrics;


  /**
   * Constructor.
   *
   * @param nodeTenantTableEntryServiceEngine to get the node connections.
   * @param cachingTenantResourceRangeEngine  to get node configuration data.
   * @param ringEngine                        to hash the data.
   * @param clock                             for timestamps.
   * @param metrics                           for processing.
   */
  @Inject
  public TableEntryManager(final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine,
                           final CachingTenantResourceRangeEngine cachingTenantResourceRangeEngine,
                           final RingEngine ringEngine,
                           final Clock clock,
                           final Metrics metrics) {
    this.nodeTenantTableEntryServiceEngine = nodeTenantTableEntryServiceEngine;
    this.cachingTenantResourceRangeEngine = cachingTenantResourceRangeEngine;
    this.ringEngine = ringEngine;
    this.clock = clock;
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
  public Optional<EntryInfo> getTenantTableEntry(final TenantResource tenantResource,
                                                 final String entry) {
    LOGGER.trace("getTenantTableEntry({},{})", tenantResource, entry);
    // get the node lists from etcd.
    final Map<NodeRange, Integer> rangeHashMap = nodeRangeToHash(tenantResource, entry);
    // TODO: fan out the reads concurrently... be smarter here.
    // TODO: This is bad below... but just to get us started.
    for (NodeRange nodeRange : rangeHashMap.keySet()) {
      final Optional<EntryInfo> result =
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
    final Map<NodeRange, Integer> rangeHashMap = nodeRangeToHash(tenantResource, entry);
    final Long timestamp = clock.millis();
    // TODO: fan out the reads concurrently... be smarter here.
    for (Map.Entry<NodeRange, Integer> tuple : rangeHashMap.entrySet()) {
      LOGGER.trace("Processing {}", tuple);
      final EntryInfo entryInfo = ImmutableEntryInfo.builder().id(entry).data(data).locationHash(tuple.getValue())
          .timestamp(timestamp).build();
      nodeTenantTableEntryServiceEngine.get(tuple.getKey())
          .createTenantTableEntry(
              tenantResource.tenant(),
              tenantResource.resource(),
              entry,
              entryInfo);
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
    final Map<NodeRange, Integer> rangeHashMap = nodeRangeToHash(tenantResource, entry);
    // TODO: fan out the reads concurrently... be smarter here.
    // TODO: This is bad below... but just to get us started.
    for (NodeRange nodeRange : rangeHashMap.keySet()) {
      nodeTenantTableEntryServiceEngine.get(nodeRange)
          .deleteTenantTableEntry(
              tenantResource.tenant(),
              tenantResource.resource(),
              entry);
    }
  }


  private Map<NodeRange, Integer> nodeRangeToHash(final TenantResource tenantResource,
                                                  final String entry) {
    return metrics.time("TableEntryManager.nodeRangeToHash", () -> {
      final TenantResourceRange range = cachingTenantResourceRangeEngine.readTenantResourceRange(tenantResource)
          .orElseThrow(NotFoundException::new);
      final RingEntry ringEntry = ringEngine.ringEntry(entry);
      return ringEntry.locationStores().stream()
          .map(hash -> Map.entry(nodeRangeForHash(range, hash), hash))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
              (oldValue, newValue) -> oldValue)); // if two have the same value, pick the old one... but we should warn.
    });
  }

  /**
   * Return the node closest (smaller than) the hash. We should time this and see if we can speed it up. It will
   * be called a lot.
   *
   * @param range for the table.
   * @param hash  we are looking for.
   * @return the node range that works.
   */
  private NodeRange nodeRangeForHash(final TenantResourceRange range, final Integer hash) {
    final Map<Integer, NodeRange> hashNodeRangeMap = range.hashToNodeRange();
    return hashNodeRangeMap.entrySet().stream()
        .filter(e -> (e.getKey() <= hash)) // remove the values with a low-hash higher than our hash.
        .max(Map.Entry.comparingByKey()) // low to high
        .map(Map.Entry::getValue)
        .orElseThrow(() -> new IllegalStateException("Unable to find correct set!"));
  }

}
