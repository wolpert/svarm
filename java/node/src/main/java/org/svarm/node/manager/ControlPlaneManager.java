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

package org.svarm.node.manager;

import org.svarm.node.model.TenantTableIdentifier;

/**
 * Provides methods to talk to the control plane.
 */
public interface ControlPlaneManager {

  /**
   * Returns the key base64 encoded for this node.
   *
   * @return the key.
   */
  String keyForNode();

  /**
   * Returns the key base64 encoded for this tenant.
   *
   * @param tenantId to use.
   * @return the key.
   */
  String keyForTenant(String tenantId);

  /**
   * Enables the table in the control plane.
   *
   * @param identifier to enable.
   */
  void enable(TenantTableIdentifier identifier);

  /**
   * Lets the control plane node we are deleted.
   *
   * @param identifier that was deleted.
   */
  void delete(TenantTableIdentifier identifier);

}
