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

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.codeheadsystems.dstore.node.manager.TenantTableEntryManager;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource for the tenant requests. (Control plane)
 */
@Singleton
@Path("/v1/tenant/{tenant}/table/{table}/entry")
public class TenantTableEntryResource implements JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableEntryResource.class);

  private final TenantTableEntryManager tenantTableEntryManager;

  /**
   * Default constructor.
   *
   * @param tenantTableEntryManager to manage the tenant table.
   */
  @Inject
  public TenantTableEntryResource(final TenantTableEntryManager tenantTableEntryManager) {
    LOGGER.info("TenantTableEntryResource({})", tenantTableEntryManager);
    this.tenantTableEntryManager = tenantTableEntryManager;
  }

  /**
   * Gets the entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    the entry.
   * @return response.
   */
  @GET
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/{entry}")
  @Produces(MediaType.APPLICATION_JSON)
  public Optional<JsonNode> read(@PathParam("tenant") final String tenantId,
                                 @PathParam("table") final String table,
                                 @PathParam("entry") final String entry) {
    LOGGER.debug("read({},{},{})", tenantId, table, entry);
    return tenantTableEntryManager.read(TenantTableIdentifier.from(tenantId, table), entry);
  }

  /**
   * Create the tenant table entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    for the table.
   * @param data     the data we care about in JSON form.
   * @return response.
   */
  @PUT
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/{entry}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response write(@PathParam("tenant") final String tenantId,
                        @PathParam("table") final String table,
                        @PathParam("entry") final String entry,
                        @NotNull @Valid final JsonNode data) { // Do NOT log this data!
    LOGGER.debug("write({},{},{})", tenantId, table, entry);
    tenantTableEntryManager.write(TenantTableIdentifier.from(tenantId, table), entry, data);
    return Response.noContent().build();
  }

  /**
   * Delete the tenant table entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    to delete.
   * @return response.
   */
  @DELETE
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/{entry}")
  public Response delete(@PathParam("tenant") final String tenantId,
                         @PathParam("table") final String table,
                         @PathParam("entry") final String entry) {
    LOGGER.debug("delete({},{},{})", tenantId, table, entry);
    if (tenantTableEntryManager.delete(TenantTableIdentifier.from(tenantId, table), entry)) {
      return Response.noContent().build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

}
