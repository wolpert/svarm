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
import java.util.Map;
import org.immutables.value.Value;

/**
 * Identifies the tenant with it's resource.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTenantResourceRange.class)
@JsonDeserialize(builder = ImmutableTenantResourceRange.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TenantResourceRange {

  /**
   * The tenant.
   *
   * @return value.
   */
  @JsonProperty("tenant")
  String tenant();

  /**
   * The resource.
   *
   * @return value.
   */
  @JsonProperty("resource")
  String resource();

  /**
   * Map where the key is the 'low' value for the range, and the value is
   * the set of nodes that should have that data.
   *
   * @return value.
   */
  @JsonProperty("hashToNodeRange")
  Map<Integer, NodeRange> hashToNodeRange();

}
