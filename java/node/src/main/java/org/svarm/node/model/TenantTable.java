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

import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Meta data about a tenant table.
 */
@Value.Immutable
public interface TenantTable {

  /**
   * The identifier for this tenant table.
   *
   * @return identifier.
   */
  TenantTableIdentifier identifier();

  /**
   * The starting hash. Can be null in which it starts at the beginning.
   *
   * @return value.
   */
  @Value.Auxiliary
  Optional<String> hash();

  /**
   * Number of rows in the table, estimated.
   *
   * @return value.
   */
  @Value.Auxiliary
  Integer estimatedQuantity();

  /**
   * Identifier if the table is enabled for use. If false, can only be managed.
   *
   * @return value.
   */
  @Value.Auxiliary
  boolean enabled();

  /**
   * The version of table this requires.
   *
   * @return value.
   */
  @Value.Auxiliary
  String tableVersion();

  /**
   * Key of the tenant. This is our local key, not the key from the control plane.
   *
   * @return value.
   */
  @Value.Auxiliary
  String key();

  /**
   * Nonce of the tenant database. This is our local key.
   *
   * @return value.
   */
  @Value.Auxiliary
  String nonce();

  /**
   * Properties used to define the table. Specific to the table version.
   *
   * @return the map.
   */
  @Value.Auxiliary
  Map<String, String> properties();
}
