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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

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
   * @param tenantId         that owns the table.
   * @param resource         the resource.
   * @param resourceMetaData what is being created.
   * @return response.
   */
  @PUT
  @Path("/{resource}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  TenantResourceInfo createResource(@PathParam("tenant") final String tenantId,
                                    @PathParam("resource") final String resource,
                                    final ResourceMetaData resourceMetaData);

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