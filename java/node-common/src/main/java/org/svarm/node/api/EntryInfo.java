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

package org.svarm.node.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Wraps around a single entry needed for cluster configuration.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEntryInfo.class)
@JsonDeserialize(builder = ImmutableEntryInfo.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface EntryInfo {

  /**
   * id of the entry.
   *
   * @return value string
   */
  @JsonProperty("id")
  String id();

  /**
   * locationHash of the entry in the node.
   *
   * @return value integer
   */
  @JsonProperty("locationHash")
  Integer locationHash();

  /**
   * timestamp of the entry.
   *
   * @return value long
   */
  @JsonProperty("timestamp")
  Long timestamp();

  /**
   * data of the entry.
   *
   * @return value json node
   */
  @JsonProperty("data")
  JsonNode data();

}
