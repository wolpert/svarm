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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.api.NodeRange;
import org.svarm.node.api.NodeTenantTableEntryService;
import org.svarm.node.javaclient.factory.NodeServiceFactory;
import org.svarm.proxy.ProxyConfiguration;

/**
 * Provides a caching accessor to the node.
 */
@Singleton
public class CachingNodeTenantTableEntryServiceEngine {

  private static final Logger LOGGER = getLogger(CachingNodeTenantTableEntryServiceEngine.class);

  private final LoadingCache<String, NodeTenantTableEntryService> cache;

  /**
   * Constructor.
   *
   * @param nodeServiceFactory for getting node instances.
   * @param configuration      the proxy configuration.
   */
  @Inject
  public CachingNodeTenantTableEntryServiceEngine(final NodeServiceFactory nodeServiceFactory,
                                                  final ProxyConfiguration configuration) {
    cache = CacheBuilder.newBuilder()
        .maximumSize(configuration.getNodeTenantServiceCacheSize())
        .removalListener(this::onRemoval)
        .build(CacheLoader.from(nodeServiceFactory::nodeService));
    LOGGER.info("CachingNodeTenantTableEntryServiceEngine()");
  }

  private void onRemoval(final RemovalNotification<String, NodeTenantTableEntryService> removalNotification) {
    LOGGER.info("Removing {} reason {}", removalNotification.getKey(), removalNotification.getCause().name());
  }

  /**
   * Returns the node tenant table entry service.
   *
   * @param nodeRange that has the uri.
   * @return the service.
   */
  public NodeTenantTableEntryService get(final NodeRange nodeRange) {
    LOGGER.trace("get({})", nodeRange);
    return cache.getUnchecked(nodeRange.uri());
  }

}
