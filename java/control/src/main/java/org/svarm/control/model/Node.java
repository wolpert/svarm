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

package org.svarm.control.model;

import java.time.Instant;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Provides internal details on nodes.
 */
@Value.Immutable
public interface Node {

  /**
   * Self-identifier for the node.
   *
   * @return value. string
   */
  String uuid();

  /**
   * Create date for the registration for the node.
   *
   * @return value. instant
   */
  Instant createDate();

  /**
   * Update for this entry about the node.
   *
   * @return value. optional
   */
  Optional<Instant> updateDate();

  /**
   * Host to access the node.
   *
   * @return value. string
   */
  String host();

  /**
   * Port of the node.
   *
   * @return value. integer
   */
  Integer port();

  /**
   * Uri string.
   *
   * @return value. string
   */
  String uri();

  /**
   * The status.
   *
   * @return status. string
   */
  String status();

}
