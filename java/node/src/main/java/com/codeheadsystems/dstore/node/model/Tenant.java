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

package com.codeheadsystems.dstore.node.model;

import org.immutables.value.Value;

/**
 * Internal structure to define the tenant itself.
 */
@Value.Immutable
public interface Tenant {

  /**
   * Id of the tenant. This is global.
   *
   * @return value.
   */
  String id();

  /**
   * Uuid of the tenant. This is local to us.
   *
   * @return value.
   */
  @Value.Auxiliary
  String uuid();

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

}
