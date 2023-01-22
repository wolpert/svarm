package com.codeheadsystems.dstore.node.api;

import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * All the ways to connect to the node service for tenant calls.
 */
public interface NodeTenantService {

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

}