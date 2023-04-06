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
import static org.svarm.proxy.module.ProxyModule.NODE_SERVICE_EXECUTOR;

import com.codeheadsystems.metrics.Metrics;
import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
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
  private static final int DEFAULT_REPLICATION_FACTOR = 3;
  private final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine;
  private final CachingTenantResourceRangeEngine cachingTenantResourceRangeEngine;
  private final RingEngine ringEngine;
  private final Clock clock;
  private final Metrics metrics;
  private final ExecutorService nodeServiceExecutor;


  /**
   * Constructor.
   *
   * @param nodeTenantTableEntryServiceEngine to get the node connections.
   * @param cachingTenantResourceRangeEngine  to get node configuration data.
   * @param ringEngine                        to hash the data.
   * @param clock                             for timestamps.
   * @param metrics                           for processing.
   * @param nodeServiceExecutor               for making requests.
   */
  @Inject
  public TableEntryManager(final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine,
                           final CachingTenantResourceRangeEngine cachingTenantResourceRangeEngine,
                           final RingEngine ringEngine,
                           final Clock clock,
                           final Metrics metrics,
                           @Named(NODE_SERVICE_EXECUTOR) ExecutorService nodeServiceExecutor) {
    this.nodeTenantTableEntryServiceEngine = nodeTenantTableEntryServiceEngine;
    this.cachingTenantResourceRangeEngine = cachingTenantResourceRangeEngine;
    this.ringEngine = ringEngine;
    this.clock = clock;
    this.metrics = metrics;
    this.nodeServiceExecutor = nodeServiceExecutor;
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
    final Map<NodeRange, Integer> rangeHashMap = nodeRangeToHash(tenantResource, entry);
    // TODO: Verify if a fan out makes sense, considering this could be a high-hit call.
    // TODO: This is bad below... it finds the first result and returns. We should have quorum reads.
    for (NodeRange nodeRange : rangeHashMap.keySet()) {
      try {
        final Optional<EntryInfo> result =
            nodeTenantTableEntryServiceEngine.get(nodeRange)
                .readTenantTableEntry(
                    tenantResource.tenant(),
                    tenantResource.resource(),
                    entry);
        if (result.isPresent()) {
          return result;
        }
      } catch (FeignException.NotFound e) {
        LOGGER.trace("Not found for {}", nodeRange);
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

    rangeHashMap.entrySet().stream()
        .map(tuple -> (Runnable) () -> {
          final EntryInfo entryInfo = ImmutableEntryInfo.builder().id(entry).data(data).locationHash(tuple.getValue())
              .timestamp(timestamp).build();
          nodeTenantTableEntryServiceEngine.get(tuple.getKey())
              .createTenantTableEntry(
                  tenantResource.tenant(),
                  tenantResource.resource(),
                  entry,
                  entryInfo);
        })
        .map(nodeServiceExecutor::submit)
        .forEach(future -> {
          try {
            future.get();
          } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Unable to save value to node.", e);
            throw new RuntimeException(e);
          }
        });
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
    // TODO: Verify if a fan out makes sense, considering this could be a high-hit call.
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
      final RingEntry ringEntry = ringEngine.ringEntry(entry, DEFAULT_REPLICATION_FACTOR);
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
