package com.codeheadsystems.dstore.node.client;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * All the ways to connect to the node service.
 */
public interface NodeService {

  // Tenants

  /**
   * Lists the tenants we have.
   *
   * @return response.
   */
  @GET
  @Path("/v1/tenant/")
  @Produces(MediaType.APPLICATION_JSON)
  Response listTenants();

  /**
   * Gets the tenant.
   *
   * @param tenantId to get.
   * @return response.
   */
  @GET
  @Path("/v1/tenant/{tenant}")
  Response readTenant(@PathParam("tenant") final String tenantId);

  /**
   * Create the tenant.
   *
   * @param tenantId to create.
   * @return response.
   */
  @PUT
  @Path("/v1/tenant/{tenant}")
  @Produces(MediaType.APPLICATION_JSON)
  Response createTenant(@PathParam("tenant") final String tenantId);

  /**
   * Delete the tenant.
   *
   * @param tenantId to delete.
   * @return response.
   */
  @DELETE
  @Path("/v1/tenant/{tenant}")
  Response deleteTenant(@PathParam("tenant") final String tenantId);

  // Tenant Tables

  /**
   * Lists the tables for the tenant we have.
   *
   * @param tenantId the tenant to search for.
   * @return response.
   */
  @GET
  @Path("/v1/tenant/{tenant}/table/")
  @Produces(MediaType.APPLICATION_JSON)
  Response listTenantTables(@PathParam("tenant") final String tenantId);

  /**
   * Gets the tenant.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @return response.
   */
  @GET
  @Path("/v1/tenant/{tenant}/table/{table}")
  Response readTenantTable(@PathParam("tenant") final String tenantId,
                           @PathParam("table") final String table);

  /**
   * Create the tenant.
   *
   * @param tenantId   that owns the table.
   * @param table      the table.
   * @param primaryKey for the table.
   * @return response.
   */
  @PUT
  @Path("/v1/tenant/{tenant}/table/{table}")
  @Produces(MediaType.APPLICATION_JSON)
  Response createTenantTable(@PathParam("tenant") final String tenantId,
                             @PathParam("table") final String table,
                             @NotNull @QueryParam("primaryKey") final String primaryKey);

  /**
   * Delete the tenant table.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @return response.
   */
  @DELETE
  @Path("/v1/tenant/{tenant}/table/{table}")
  Response deleteTenantTable(@PathParam("tenant") final String tenantId,
                             @PathParam("table") final String table);

  // Tenant Table Entry


  /**
   * Gets the entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    the entry.
   * @return response.
   */
  @GET
  @Path("/v1/tenant/{tenant}/table/{table}/entry//{entry}")
  Response readTenantTableEntry(@PathParam("tenant") final String tenantId,
                                @PathParam("table") final String table,
                                @PathParam("entry") final String entry);

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
  @Path("/v1/tenant/{tenant}/table/{table}/entry//{entry}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Response createTenantTableEntry(@PathParam("tenant") final String tenantId,
                                  @PathParam("table") final String table,
                                  @PathParam("entry") final String entry,
                                  @NotNull @Valid final JsonNode data);

  /**
   * Delete the tenant table entry.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param entry    to delete.
   * @return response.
   */
  @DELETE
  @Path("/v1/tenant/{tenant}/table/{table}/entry//{entry}")
  Response deleteTenantTableEntry(@PathParam("tenant") final String tenantId,
                                  @PathParam("table") final String table,
                                  @PathParam("entry") final String entry);
}