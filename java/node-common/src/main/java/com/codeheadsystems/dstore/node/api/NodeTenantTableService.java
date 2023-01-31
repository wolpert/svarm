package com.codeheadsystems.dstore.node.api;

import java.util.List;
import java.util.Optional;
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
   * @param tenantId   that owns the table.
   * @param table      the table.
   * @return response.
   */
  @PUT
  @Path("/{table}")
  @Produces(MediaType.APPLICATION_JSON)
  TenantTableInfo createTenantTable(@PathParam("tenant") final String tenantId,
                                    @PathParam("table") final String table);

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