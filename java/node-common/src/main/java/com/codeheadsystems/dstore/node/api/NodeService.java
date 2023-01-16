package com.codeheadsystems.dstore.node.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
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
  List<String> listTenants();

  /**
   * Gets the tenant.
   *
   * @param tenantId to get.
   * @return response.
   */
  @GET
  @Path("/v1/tenant/{tenant}")
  TenantInfo readTenant(@PathParam("tenant") final String tenantId);

  /**
   * Create the tenant.
   *
   * @param tenantId to create.
   * @return response.
   */
  @PUT
  @Path("/v1/tenant/{tenant}")
  @Produces(MediaType.APPLICATION_JSON)
  TenantInfo createTenant(@PathParam("tenant") final String tenantId);

  /**
   * Delete the tenant.
   *
   * @param tenantId to delete.
   */
  @DELETE
  @Path("/v1/tenant/{tenant}")
  void deleteTenant(@PathParam("tenant") final String tenantId);

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
  List<String> listTenantTables(@PathParam("tenant") final String tenantId);

  /**
   * Gets the tenant.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @return response.
   */
  @GET
  @Path("/v1/tenant/{tenant}/table/{table}")
  TenantTableInfo readTenantTable(@PathParam("tenant") final String tenantId,
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
  TenantTableInfo createTenantTable(@PathParam("tenant") final String tenantId,
                                    @PathParam("table") final String table,
                                    @NotNull @QueryParam("primaryKey") final String primaryKey);

  /**
   * Delete the tenant table.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   */
  @DELETE
  @Path("/v1/tenant/{tenant}/table/{table}")
  void deleteTenantTable(@PathParam("tenant") final String tenantId,
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
  @Path("/v1/tenant/{tenant}/table/{table}/entry//{entry}")
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
  @Path("/v1/tenant/{tenant}/table/{table}/entry//{entry}")
  void deleteTenantTableEntry(@PathParam("tenant") final String tenantId,
                              @PathParam("table") final String table,
                              @PathParam("entry") final String entry);
}