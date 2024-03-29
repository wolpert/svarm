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

package org.svarm.node.javaclient.factory;

import static org.slf4j.LoggerFactory.getLogger;

import feign.Feign;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.github.resilience4j.retry.Retry;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.javaclient.FeignBuilderInstrumentator;
import org.svarm.node.api.NodeTenantTableEntryService;

/**
 * Creates instances of the node service based on the uri. No caching here.
 */
@Singleton
public class NodeServiceFactory {

  private static final Logger LOGGER = getLogger(NodeServiceFactory.class);

  private final Feign.Builder builder;


  /**
   * Constructor.
   *
   * @param instrumentator to instrument.
   * @param retry          default retry policy.
   */
  @Inject
  public NodeServiceFactory(final FeignBuilderInstrumentator instrumentator,
                            @Named("DEFAULT") final Retry retry) {
    final FeignDecorators decorators = FeignDecorators.builder().withRetry(retry).build();
    this.builder = Resilience4jFeign.builder(decorators);
    instrumentator.instrument(builder);
    LOGGER.info("NodeServiceFactory({})", builder);
  }

  /**
   * Get a usable node service.
   *
   * @param uri the url of the node service.
   * @return a node service.
   */
  public NodeTenantTableEntryService nodeService(final String uri) {
    LOGGER.info("nodeService({})", uri);
    return builder.target(NodeTenantTableEntryService.class, uri);
  }

}
