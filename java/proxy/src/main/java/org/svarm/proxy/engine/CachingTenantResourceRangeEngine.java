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

package org.svarm.proxy.engine;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.api.TenantResourceRange;
import org.svarm.common.config.engine.NodeConfigurationEngine;
import org.svarm.common.config.engine.WatchEngine;
import org.svarm.common.config.factory.WatchEngineFactory;
import org.svarm.proxy.ProxyConfiguration;
import org.svarm.server.exception.NotFoundException;

/**
 * This will cache the tenant resource range.
 */
@Singleton
public class CachingTenantResourceRangeEngine {

  private static final Logger LOGGER = getLogger(CachingTenantResourceRangeEngine.class);

  private final NodeConfigurationEngine nodeConfigurationEngine;
  private final WatchEngineFactory watchEngineFactory;
  private final LoadingCache<TenantResource, CacheHolder> cache;

  /**
   * Constructor.
   *
   * @param nodeConfigurationEngine to get the configuration.
   * @param watchEngineFactory      to watch for changes.
   * @param proxyConfiguration      for configuration.
   */
  @Inject
  public CachingTenantResourceRangeEngine(final NodeConfigurationEngine nodeConfigurationEngine,
                                          final WatchEngineFactory watchEngineFactory,
                                          final ProxyConfiguration proxyConfiguration) {
    this.nodeConfigurationEngine = nodeConfigurationEngine;
    this.watchEngineFactory = watchEngineFactory;
    this.cache = CacheBuilder.newBuilder()
        .maximumSize(proxyConfiguration.getTenantResourceRangeCacheSize())
        .removalListener(this::onRemoval)
        .build(CacheLoader.from(this::generate));
    LOGGER.info("CachingTenantResourceRangeEngine()");
  }

  private CacheHolder generate(final TenantResource tenantResource) {
    final TenantResourceRange range = nodeConfigurationEngine.readTenantResourceRange(tenantResource)
        .orElseThrow(NotFoundException::new);
    final WatchEngine watchEngine = watchEngineFactory.watchEngine(
        NodeConfigurationEngine.TENANT_NAMESPACE,
        nodeConfigurationEngine.getTenantResourceKey(tenantResource),
        (e) -> clear(tenantResource));
    return ImmutableCacheHolder.builder().tenantResourceRange(range).watchEngine(watchEngine).build();
  }

  private void clear(final TenantResource tenantResource) {
    LOGGER.trace("clear({})", tenantResource);
    final CacheHolder cacheHolder = cache.getIfPresent(tenantResource);
    if (cacheHolder != null) {
      cacheHolder.watchEngine().close();
      cache.invalidate(tenantResource);
    } else {
      LOGGER.warn("Got a watcher with no value: {}", tenantResource);
    }
  }

  private void onRemoval(
      final RemovalNotification<TenantResource, CachingTenantResourceRangeEngine.CacheHolder> notification) {
    LOGGER.trace("removed {} because {}", notification.getKey(), notification.getCause().name());
    notification.getValue().watchEngine().close();
  }

  /**
   * Reads all the configuration for the tenant resource.
   *
   * @param tenantResource of the node.
   * @return the list of resource ranges.
   */
  public Optional<TenantResourceRange> readTenantResourceRange(final TenantResource tenantResource) {
    LOGGER.trace("readTenantResourceRange", tenantResource);
    try {
      return Optional.of(cache.get(tenantResource).tenantResourceRange());
    } catch (ExecutionException e) {
      LOGGER.trace("Not found {}", tenantResource);
      return Optional.empty();
    }
  }

  /**
   * Holds the various components for the cache.
   */
  @Value.Immutable
  interface CacheHolder {

    /**
     * The range itself.
     *
     * @return the range.
     */
    TenantResourceRange tenantResourceRange();

    /**
     * The watcher.
     *
     * @return watcher. watch engine
     */
    WatchEngine watchEngine();

  }

}
