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

package org.svarm.node.model;

import org.immutables.value.Value;

/**
 * Identifier for a tenant table.
 */
@Value.Immutable
public interface TenantTableIdentifier {

  /**
   * Helper method to create an identifier.
   *
   * @param tenantId  to use.
   * @param tableName to use.
   * @return the identifier.
   */
  static TenantTableIdentifier from(final String tenantId, final String tableName) {
    return ImmutableTenantTableIdentifier.builder().tenantId(tenantId).tableName(tableName).build();
  }

  /**
   * Identifier of the tenant.
   *
   * @return value. string
   */
  String tenantId();

  /**
   * Name of the table.
   *
   * @return value. string
   */
  String tableName();

}
