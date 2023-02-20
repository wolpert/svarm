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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Provides details on the node range setup.
 */
@Value.Immutable
public interface NodeRange {

  /**
   * Accessor for uuid.
   *
   * @return the value.
   */
  @JsonProperty("nodeUuid")
  String nodeUuid();

  /**
   * Accessor for tenant.
   *
   * @return the value.
   */
  @JsonProperty("tenant")
  String tenant();

  /**
   * Accessor for resource.
   *
   * @return the value.
   */
  @JsonProperty("resource")
  String resource();

  /**
   * Accessor for createDate.
   *
   * @return the value.
   */
  @JsonProperty("createDate")
  Instant createDate();

  /**
   * Accessor for updateDate.
   *
   * @return the value.
   */
  @JsonProperty("updateDate")
  Optional<Instant> updateDate();

  /**
   * Accessor for tableVersion.
   *
   * @return the value.
   */
  @JsonProperty("tableVersion")
  String tableVersion();

  /**
   * Accessor for ready.
   *
   * @return the value.
   */
  @JsonProperty("ready")
  Boolean ready();

  /**
   * Accessor for status.
   *
   * @return the value.
   */
  @JsonProperty("status")
  String status();

  /**
   * Accessor for lowHash.
   *
   * @return the value.
   */
  @JsonProperty("lowHash")
  Integer lowHash();

  /**
   * Accessor for highHash.
   *
   * @return the value.
   */
  @JsonProperty("highHash")
  Integer highHash();
}
