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
import com.codeheadsystems.dstore.node.api.NodeTenantService;
import com.codeheadsystems.dstore.node.api.TenantInfo;
import com.codeheadsystems.dstore.node.converter.TenantInfoConverter;
import com.codeheadsystems.dstore.node.manager.TenantManager;
import com.codeheadsystems.dstore.node.model.Tenant;
import com.codeheadsystems.server.exception.NotFoundException;
import com.codeheadsystems.server.resource.JerseyResource;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource for the tenant requests. (Control plane)
 */
@Singleton
public class TenantResource implements NodeTenantService, JerseyResource {

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
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public List<String> listTenants() {
    LOGGER.debug("list()");
    return tenantManager.tenants();
  }

  /**
   * Gets the tenant.
   *
   * @param tenantId to get.
   * @return response.
   */

  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public Optional<TenantInfo> readTenant(final String tenantId) {
    LOGGER.debug("readTenant({})", tenantId);
    return tenantManager.get(tenantId)
        .map(tenantInfoConverter::from);
  }

  /**
   * Create the tenant.
   *
   * @param tenantId to create.
   * @return response.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public TenantInfo createTenant(final String tenantId) {
    LOGGER.debug("create({})", tenantId);
    final Tenant tenant = tenantManager.create(tenantId);
    return tenantInfoConverter.from(tenant);
  }


  /**
   * Delete the tenant.
   *
   * @param tenantId to delete.
   */
  @Timed
  @ExceptionMetered
  @ResponseMetered
  @Override
  public void deleteTenant(final String tenantId) {
    LOGGER.debug("delete({})", tenantId);
    if (!tenantManager.delete(tenantId)) {
      throw new NotFoundException();
    }
  }


}
