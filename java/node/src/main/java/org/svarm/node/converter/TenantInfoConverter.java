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

package org.svarm.node.converter;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.api.ImmutableTenantInfo;
import org.svarm.node.api.TenantInfo;
import org.svarm.node.model.Tenant;

/**
 * Converter for tenant info objects.
 */
@Singleton
public class TenantInfoConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantInfoConverter.class);

  /**
   * Default constructor.
   */
  @Inject
  public TenantInfoConverter() {
    LOGGER.info("TenantInfoConverter()");
  }

  /**
   * Creates a tenant info from a tenant.
   *
   * @param tenant to read from.
   * @return result.
   */
  public TenantInfo from(final Tenant tenant) {
    LOGGER.trace("from({})", tenant);
    return ImmutableTenantInfo.builder().id(tenant.ridTenant()).build();
  }

}
