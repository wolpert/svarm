package com.codeheadsystems.dstore.node.api;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * All the ways to connect to the node service for tenant table calls.
 */
public interface NodeTenantTableService {

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

}