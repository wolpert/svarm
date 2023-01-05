package com.codeheadsystems.dstore.node.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.codeheadsystems.dstore.node.api.TenantInfo;
import com.codeheadsystems.dstore.node.converter.TenantInfoConverter;
import com.codeheadsystems.dstore.node.manager.TenantManager;
import com.codeheadsystems.dstore.node.model.Tenant;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
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
@Path("/v1")
public class TenantResource implements JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantResource.class);

  private final TenantManager tenantManager;
  private final TenantInfoConverter tenantInfoConverter;

  /**
   * Default constructor.
   *
   * @param tenantManager       to manage the tenant.
   * @param tenantInfoConverter to convert objects.
   */
  @Inject
  public TenantResource(final TenantManager tenantManager,
                        final TenantInfoConverter tenantInfoConverter) {
    LOGGER.info("TenantResource({},{})", tenantManager, tenantInfoConverter);
    this.tenantInfoConverter = tenantInfoConverter;
    this.tenantManager = tenantManager;
  }

  /**
   * Lists the tenants we have.
   *
   * @return response.
   */
  @GET
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/tenant")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> list() {
    return tenantManager.tenants();
  }

  /**
   * Gets the tenant.
   *
   * @param tenant to get.
   * @return response.
   */
  @GET
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/tenant/{tenant}")
  @Produces(MediaType.APPLICATION_JSON)
  public Optional<TenantInfo> read(@PathParam("tenant") final String tenant) {
    return tenantManager.get(tenant)
        .map(tenantInfoConverter::from);
  }

  /**
   * Create the tenant.
   *
   * @param tenantId to create.
   * @return response.
   */
  @PUT
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/tenant/{tenant}")
  @Produces(MediaType.APPLICATION_JSON)
  public TenantInfo create(@PathParam("tenant") final String tenantId) {
    final Tenant tenant = tenantManager.create(tenantId);
    return tenantInfoConverter.from(tenant);
  }


  /**
   * Delete the tenant.
   *
   * @param tenant to delete.
   * @return response.
   */
  @DELETE
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/tenant/{tenant}")
  public Response delete(@PathParam("tenant") final String tenant) {
    if (tenantManager.delete(tenant)) {
      return Response.noContent().build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }


}
