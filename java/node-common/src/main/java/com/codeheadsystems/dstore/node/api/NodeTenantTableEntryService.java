package com.codeheadsystems.dstore.node.api;

import com.fasterxml.jackson.databind.JsonNode;
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
  @Path("/v1/tenant/{tenant}/table/{table}/entry/{entry}")
  @Produces(MediaType.APPLICATION_JSON)
  JsonNode readTenantTableEntry(@PathParam("tenant") final String tenantId,
                                @PathParam("table") final String table,
                                @PathParam("entry") final String entry);

  /**
   * Create the tenant table entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    for the table.
   * @param data     the data we care about in JSON form.
   */
  @PUT
  @Path("/v1/tenant/{tenant}/table/{table}/entry/{entry}")
  @Consumes(MediaType.APPLICATION_JSON)
  void createTenantTableEntry(@PathParam("tenant") final String tenantId,
                              @PathParam("table") final String table,
                              @PathParam("entry") final String entry,
                              @NotNull @Valid final JsonNode data);

  /**
   * Delete the tenant table entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    to delete.
   */
  @DELETE
  @Path("/v1/tenant/{tenant}/table/{table}/entry/{entry}")
  void deleteTenantTableEntry(@PathParam("tenant") final String tenantId,
                              @PathParam("table") final String table,
                              @PathParam("entry") final String entry);
}