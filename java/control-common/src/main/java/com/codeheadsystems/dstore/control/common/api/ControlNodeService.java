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

package com.codeheadsystems.dstore.control.common.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Defines the methods nodes use by the control plane.
 */
@Path("/v1/node")
public interface ControlNodeService {

  /**
   * Registers a given node into the swarm.
   *
   * @param nodeUuid to enable.
   * @param metaData the node details.
   * @return a node info object.
   */
  @PUT
  @Path("/{node}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  NodeInfo register(@PathParam("node") final String nodeUuid, final NodeMetaData metaData);

  /**
   * Enables a given node into the swarm.
   *
   * @param nodeUuid to enable.
   * @return a node info object.
   */
  @PUT
  @Path("/{node}/enable")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  NodeInfo enable(@PathParam("node") final String nodeUuid);

  /**
   * Disables a given node into the swarm.
   *
   * @param nodeUuid to disables.
   * @return a node info object.
   */
  @PUT
  @Path("/{node}/disable")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  NodeInfo disable(@PathParam("node") final String nodeUuid);

  /**
   * Gets the key for the node in the control plane. This is the part of the
   * key the control plane controls.
   *
   * @param nodeUuid to get the key for.
   * @return a node info object.
   */
  @GET
  @Path("/{node}/key")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  KeyInfo nodeKey(@PathParam("node") final String nodeUuid);

  /**
   * Gets the key for the node's reference for the control plane.
   * Note that usually this is the tenant or tenant table. The control plane
   * will ensure this is a) consistent, b) idempotent. This is the part of the
   * key the control plane controls.
   *
   * @param nodeUuid to get the key for.
   * @param reference the reference.
   * @return a node info object.
   */
  @GET
  @Path("/{node}/key/{reference}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  KeyInfo nodeKey(@PathParam("node") final String nodeUuid,
                  @PathParam("reference") final String reference);

}
