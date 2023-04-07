package org.svarm.proxy.engine;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.metrics.Metrics;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.api.NodeRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.api.TenantResourceRange;
import org.svarm.common.engine.RingEngine;
import org.svarm.common.model.RingEntry;
import org.svarm.server.exception.NotFoundException;

/**
 * Provides the ability to resolve node ranges for a given resource.
 */
@Singleton
public class NodeRangeResolverEngine {
  private static final int DEFAULT_REPLICATION_FACTOR = 3;
  private static final Logger LOGGER = getLogger(NodeRangeResolverEngine.class);

  private final RingEngine ringEngine;
  private final Metrics metrics;
  private final CachingTenantResourceRangeEngine cachingTenantResourceRangeEngine;

  /**
   * Constructor.
   *
   * @param ringEngine                       to hash the data.
   * @param metrics                          for processing.
   * @param cachingTenantResourceRangeEngine to get node configuration data.
   */
  @Inject
  public NodeRangeResolverEngine(final RingEngine ringEngine,
                                 final Metrics metrics,
                                 final CachingTenantResourceRangeEngine cachingTenantResourceRangeEngine) {
    this.ringEngine = ringEngine;
    this.metrics = metrics;
    this.cachingTenantResourceRangeEngine = cachingTenantResourceRangeEngine;
    LOGGER.info("NodeRangeResolverEngine({},{},{})", ringEngine, metrics, cachingTenantResourceRangeEngine);
  }

  /**
   * Given the tenantResource and the entry, return the node ranges that are responsible for the entry, mapped to
   * the hash of the entry to save.
   *
   * @param tenantResource for placement.
   * @param entry          to lookup.
   * @return the map.
   */
  public Map<NodeRange, Integer> nodeRangeToHash(final TenantResource tenantResource,
                                                 final String entry) {
    LOGGER.trace("nodeRangeToHash({}, {})", tenantResource, entry);
    return metrics.time("TableEntryManager.nodeRangeToHash", () -> {
      final TenantResourceRange range = cachingTenantResourceRangeEngine.readTenantResourceRange(tenantResource)
          .orElseThrow(NotFoundException::new);
      final RingEntry ringEntry = ringEngine.ringEntry(entry, DEFAULT_REPLICATION_FACTOR); // TODO: make this configurable.
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
  public NodeRange nodeRangeForHash(final TenantResourceRange range, final Integer hash) {
    LOGGER.trace("nodeRangeForHash({}, {})", range, hash);
    final Map<Integer, NodeRange> hashNodeRangeMap = range.hashToNodeRange();
    return hashNodeRangeMap.entrySet().stream()
        .filter(e -> (e.getKey() <= hash)) // remove the values with a low-hash higher than our hash.
        .max(Map.Entry.comparingByKey()) // low to high
        .map(Map.Entry::getValue)
        .orElseThrow(() -> new IllegalStateException("Unable to find correct set!"));
  }
}
