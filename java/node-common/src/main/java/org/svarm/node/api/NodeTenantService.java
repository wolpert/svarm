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

package org.svarm.node.api;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * All the ways to connect to the node service for tenant calls.
 */
@Path("/v1/tenant")
public interface NodeTenantService {

  /**
   * Lists the tenants we have.
   *
   * @return response.
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  List<String> listTenants();

  /**
   * Gets the tenant.
   *
   * @param tenantId to get.
   * @return response.
   */
  @GET
  @Path("/{tenant}")
  @Produces(MediaType.APPLICATION_JSON)
  Optional<TenantInfo> readTenant(@PathParam("tenant") final String tenantId);

  /**
   * Create the tenant.
   *
   * @param tenantId to create.
   * @return response.
   */
  @PUT
  @Path("/{tenant}")
  @Produces(MediaType.APPLICATION_JSON)
  TenantInfo createTenant(@PathParam("tenant") final String tenantId);

  /**
   * Delete the tenant.
   *
   * @param tenantId to delete.
   */
  @DELETE
  @Path("/{tenant}")
  void deleteTenant(@PathParam("tenant") final String tenantId);

}