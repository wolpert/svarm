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
import org.svarm.node.api.ImmutableTenantTableInfo;
import org.svarm.node.api.TenantTableInfo;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * Converter for tenant table info objects.
 */
@Singleton
public class TenantTableInfoConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableInfoConverter.class);

  /**
   * Default constructor.
   */
  @Inject
  public TenantTableInfoConverter() {
    LOGGER.info("TenantTableInfoConverter()");
  }

  /**
   * Creates a tenant table info from a tenant table identifier.
   *
   * @param identifier to read from.
   * @return result.
   */
  public TenantTableInfo from(final TenantTableIdentifier identifier) {
    LOGGER.trace("from({})", identifier);
    return ImmutableTenantTableInfo.builder().id(identifier.tableName()).build();
  }

}
