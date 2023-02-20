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

package org.svarm.control.common.api;

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
 * All the ways to connect to the node service for tenant table calls.
 */
@Path("/v1/tenant/{tenant}/resource")
public interface ControlTenantResourceService {

  /**
   * Lists the tables for the tenant we have.
   *
   * @param tenantId the tenant to search for.
   * @return response.
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  List<String> listResources(@PathParam("tenant") final String tenantId);

  /**
   * Gets the tenant.
   *
   * @param tenantId that owns the resource.
   * @param resource the resource.
   * @return response.
   */
  @GET
  @Path("/{resource}")
  @Produces(MediaType.APPLICATION_JSON)
  Optional<TenantResourceInfo> readResource(@PathParam("tenant") final String tenantId,
                                            @PathParam("resource") final String resource);

  /**
   * Create the tenant.
   *
   * @param tenantId that owns the table.
   * @param resource the resource.
   * @return response.
   */
  @PUT
  @Path("/{resource}")
  @Produces(MediaType.APPLICATION_JSON)
  TenantResourceInfo createResource(@PathParam("tenant") final String tenantId,
                                    @PathParam("resource") final String resource);

  /**
   * Delete the tenant table.
   *
   * @param tenantId that owns the table.
   * @param resource the table.
   */
  @DELETE
  @Path("/{resource}")
  void deleteResource(@PathParam("tenant") final String tenantId,
                      @PathParam("resource") final String resource);

}