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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Optional;

/**
 * All the ways to connect to the node service for tenant table entry calls.
 */
@Path("/v1/tenant/{tenant}/table/{table}/entry")
public interface NodeTenantTableEntryService {

  /**
   * Gets the entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    the entry.
   * @return response. optional
   */
  @GET
  @Path("/{entry}")
  @Produces(MediaType.APPLICATION_JSON)
  Optional<EntryInfo> readTenantTableEntry(@PathParam("tenant") final String tenantId,
                                           @PathParam("table") final String table,
                                           @PathParam("entry") final String entry);

  /**
   * Create the tenant table entry.
   *
   * @param tenantId  that owns the table.
   * @param table     the table.
   * @param entry     for the table.
   * @param entryInfo the data we care about in JSON form.
   */
  @PUT
  @Path("/{entry}")
  @Consumes(MediaType.APPLICATION_JSON)
  void createTenantTableEntry(@PathParam("tenant") final String tenantId,
                              @PathParam("table") final String table,
                              @PathParam("entry") final String entry,
                              @NotNull @Valid final EntryInfo entryInfo);

  /**
   * Delete the tenant table entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    to delete.
   */
  @DELETE
  @Path("/{entry}")
  void deleteTenantTableEntry(@PathParam("tenant") final String tenantId,
                              @PathParam("table") final String table,
                              @PathParam("entry") final String entry);
}