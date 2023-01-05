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

package com.codeheadsystems.dstore.node.resource;

import com.codahale.metrics.annotation.Timed;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.codeheadsystems.metrics.Metrics;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Ping.
 */
@Singleton
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public class PingResource implements JerseyResource {

  private final Metrics metrics;

  /**
   * Default constructor.
   *
   * @param configuration         to use.
   * @param internalConfiguration to use.
   * @param metrics               to use.
   */
  @Inject
  public PingResource(final NodeConfiguration configuration,
                      final NodeInternalConfiguration internalConfiguration,
                      final Metrics metrics) {
    this.metrics = metrics;
  }

  /**
   * Ping!
   *
   * @return pong.
   */
  @GET
  @Timed
  @Path("/ping")
  public String ping() {
    return "Pong";
  }

}
