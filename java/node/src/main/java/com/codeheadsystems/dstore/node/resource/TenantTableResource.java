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
import com.codeheadsystems.dstore.node.api.TenantTableInfo;
import com.codeheadsystems.dstore.node.converter.TenantTableInfoConverter;
import com.codeheadsystems.dstore.node.engine.TableDefinitionEngine;
import com.codeheadsystems.dstore.node.manager.TenantTableManager;
import com.codeheadsystems.dstore.node.model.TenantTable;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource for the tenant requests. (Control plane)
 */
@Singleton
@Path("/v1/tenant/{tenant}/table")
public class TenantTableResource implements JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableResource.class);

  private final TenantTableManager tenantTableManager;
  private final TenantTableInfoConverter converter;

  /**
   * Default constructor.
   *
   * @param tenantTableManager to manage the tenant table.
   * @param converter          converter.
   */
  @Inject
  public TenantTableResource(final TenantTableManager tenantTableManager,
                             final TenantTableInfoConverter converter) {
    LOGGER.info("TenantResource({}, {})", tenantTableManager, converter);
    this.tenantTableManager = tenantTableManager;
    this.converter = converter;
  }

  /**
   * Lists the tables for the tenant we have.
   *
   * @param tenantId the tenant to search for.
   * @return response.
   */
  @GET
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> list(@PathParam("tenant") final String tenantId) {
    return tenantTableManager.tables(tenantId);
  }

  /**
   * Gets the tenant.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @return response.
   */
  @GET
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/{table}")
  @Produces(MediaType.APPLICATION_JSON)
  public Optional<TenantTableInfo> read(@PathParam("tenant") final String tenantId,
                                        @PathParam("table") final String table) {
    return tenantTableManager.get(tenantId, table)
        .map(TenantTable::identifier)
        .map(converter::from);
  }

  /**
   * Create the tenant.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @param primaryKey for the table.
   * @return response.
   */
  @PUT
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/{table}")
  @Produces(MediaType.APPLICATION_JSON)
  public TenantTableInfo create(@PathParam("tenant") final String tenantId,
                                @PathParam("table") final String table,
                                @QueryParam("primaryKey") final String primaryKey) {
    final TenantTable tenantTable = tenantTableManager
        .create(tenantId, table, TableDefinitionEngine.V1SingleEntryEngine.DEFINITION_NAME, primaryKey);
    return converter.from(tenantTable.identifier());
  }


  /**
   * Delete the tenant table.
   *
   * @param tenantId that owns the table.
   * @param table    the table.
   * @return response.
   */
  @DELETE
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Path("/{table}")
  public Response delete(@PathParam("tenant") final String tenantId,
                         @PathParam("table") final String table) {
    if (tenantTableManager.delete(tenantId, table)) {
      return Response.noContent().build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }


}
