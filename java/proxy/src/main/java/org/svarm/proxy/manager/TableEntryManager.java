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
import com.google.common.annotations.VisibleForTesting;
import feign.FeignException;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.api.NodeRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.api.ImmutableEntryInfo;
import org.svarm.proxy.engine.NodeRangeResolverEngine;
import org.svarm.proxy.engine.NodeTenantTableEntryServiceEngine;

/**
 * Handles the requests to the various nodes for a single entry.
 */
@Singleton
public class TableEntryManager {

  private static final Logger LOGGER = getLogger(TableEntryManager.class);
  private static final int DEFAULT_REPLICATION_FACTOR = 3;
  private final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine;
  private final Clock clock;
  private final Metrics metrics;
  private final ExecutorService nodeServiceExecutor;
  private final NodeRangeResolverEngine nodeRangeResolverEngine;


  /**
   * Constructor.
   *
   * @param nodeTenantTableEntryServiceEngine to get the node connections.
   * @param clock                             for timestamps.
   * @param metrics                           for processing.
   * @param nodeServiceExecutor               for making requests.
   * @param nodeRangeResolverEngine           to get the node ranges.
   */
  @Inject
  public TableEntryManager(final NodeTenantTableEntryServiceEngine nodeTenantTableEntryServiceEngine,
                           final Clock clock,
                           final Metrics metrics,
                           final @Named(NODE_SERVICE_EXECUTOR) ExecutorService nodeServiceExecutor,
                           final NodeRangeResolverEngine nodeRangeResolverEngine) {
    this.nodeTenantTableEntryServiceEngine = nodeTenantTableEntryServiceEngine;
    this.clock = clock;
    this.metrics = metrics;
    this.nodeServiceExecutor = nodeServiceExecutor;
    this.nodeRangeResolverEngine = nodeRangeResolverEngine;
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
    final Map<NodeRange, Integer> rangeHashMap = nodeRangeResolverEngine.nodeRangeToHash(tenantResource, entry);

    final Map<JsonNode, List<EntryInfo>> map = rangeHashMap.keySet().stream()
        .map(nodeRange -> (Callable<Optional<EntryInfo>>) () -> getEntryFromNode(tenantResource, entry, nodeRange))
        .map(nodeServiceExecutor::submit)
        .map(this::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.groupingBy(EntryInfo::data));
    if (map.isEmpty()) {
      return Optional.empty();
    } else {
      return map.entrySet().stream()
          .sorted(this::largestListFirst)
          .map(Map.Entry::getValue)
          .map(list -> list.get(0))
          .findFirst();
    }
  }

  @VisibleForTesting
  int largestListFirst(final Map.Entry<JsonNode, List<EntryInfo>> entry1,
                       final Map.Entry<JsonNode, List<EntryInfo>> entry2) {
    return entry2.getValue().size() - entry1.getValue().size();
  }

  private <T> T get(final Future<T> future) {
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Unable to get value", e);
      throw new IllegalStateException("Unable to get value", e);
    }
  }

  private Optional<EntryInfo> getEntryFromNode(final TenantResource tenantResource, final String entry, final NodeRange nodeRange) {
    try {
      return nodeTenantTableEntryServiceEngine.get(nodeRange)
          .readTenantTableEntry(
              tenantResource.tenant(),
              tenantResource.resource(),
              entry);
    } catch (FeignException.NotFound e) {
      LOGGER.trace("Not found for {}", nodeRange);
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
    final Map<NodeRange, Integer> rangeHashMap = nodeRangeResolverEngine.nodeRangeToHash(tenantResource, entry);
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
        .forEach(this::get); // let the future complete before we return.
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
    final Map<NodeRange, Integer> rangeHashMap = nodeRangeResolverEngine.nodeRangeToHash(tenantResource, entry);
    rangeHashMap.keySet().stream()
        .map(nodeTenantTableEntryServiceEngine::get)
        .map((node) -> (Runnable) () -> node.deleteTenantTableEntry(
            tenantResource.tenant(),
            tenantResource.resource(),
            entry))
        .map(nodeServiceExecutor::submit)
        .forEach(this::get); // let the future complete before we return.
  }


}
