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

package org.svarm.control.common.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Provides details on the node info.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNodeInfo.class)
@JsonDeserialize(builder = ImmutableNodeInfo.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface NodeInfo {

  /**
   * The UUID of the node.
   *
   * @return value. string
   */
  @JsonProperty("uuid")
  String uuid();


  /**
   * The Status of the node.
   *
   * @return value. string
   */
  @JsonProperty("status")
  String status();

  /**
   * Status objects. We do not use real enums in transport do deserialization won't fail.
   * But there are the ones that are known.
   */
  enum Status {
    /**
     * The node is unknown to the swarm.
     */
    UNKNOWN,
    /**
     * The node is enabled.
     */
    ENABLED,
    /**
     * The node is disabled.
     */
    DISABLED,
    /**
     * The node will always be rejected.
     */
    BANNED
  }

}
