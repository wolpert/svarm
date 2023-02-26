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

package org.svarm.common.config.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * has range, inclusive start, exclusive end.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaData.class)
@JsonDeserialize(builder = ImmutableMetaData.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface MetaData {

  /**
   * The low value of the hash range, inclusive.
   *
   * @return value.
   */
  @JsonProperty("lowHash")
  Integer lowHash();

  /**
   * The high value of the hash range, exclusive.
   *
   * @return value.
   */
  @JsonProperty("highHash")
  Integer highHash();

}
