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

package com.codeheadsystems.dstore.proxy.factory;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.node.api.NodeTenantTableEntryService;
import feign.Feign;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

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
   * @param builder for the feign client.
   */
  @Inject
  public NodeServiceFactory(final Feign.Builder builder) {
    this.builder = builder;
    LOGGER.info("NodeServiceFactory()");
  }

  /**
   * Get a usable node service.
   *
   * @param uri the url of the node service.
   * @return a node service.
   */
  public NodeTenantTableEntryService nodeService(final String uri) {
    LOGGER.info("nodeService({})", uri);
    final String url = String.format("http://%s", uri);
    return builder.target(NodeTenantTableEntryService.class, url);
  }

}
