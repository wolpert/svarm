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
import javax.ws.rs.Consumes;
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
@Path("/v1/tenant/{tenant}/table")
public interface NodeTenantTableService {

  /**
   * Lists the tables for the tenant we have.
   *
   * @param tenantId the tenant to search for.
   * @return response.
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  List<String> listTenantTables(@PathParam("tenant") final String tenantId);

  /**
   * Gets the tenant.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @return response.
   */
  @GET
  @Path("/{table}")
  @Produces(MediaType.APPLICATION_JSON)
  Optional<TenantTableInfo> readTenantTable(@PathParam("tenant") final String tenantId,
                                            @PathParam("table") final String table);

  /**
   * Create the tenant.
   *
   * @param tenantId      that owns the table.
   * @param table         the table.
   * @param tableMetaData for the table.
   * @return response.
   */
  @PUT
  @Path("/{table}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  TenantTableInfo createTenantTable(@PathParam("tenant") final String tenantId,
                                    @PathParam("table") final String table,
                                    final TableMetaData tableMetaData);

  /**
   * Delete the tenant table.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   */
  @DELETE
  @Path("/{table}")
  void deleteTenantTable(@PathParam("tenant") final String tenantId,
                         @PathParam("table") final String table);

}