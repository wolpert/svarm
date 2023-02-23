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

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
   * @return response.
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