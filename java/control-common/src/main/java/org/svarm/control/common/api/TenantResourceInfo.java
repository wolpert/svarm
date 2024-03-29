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
 * Provides details on the resource info.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTenantResourceInfo.class)
@JsonDeserialize(builder = ImmutableTenantResourceInfo.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TenantResourceInfo {

  /**
   * The tenantId.
   *
   * @return value. string
   */
  @JsonProperty("tenantId")
  String tenantId();

  /**
   * The resource.
   *
   * @return value. string
   */
  @JsonProperty("resource")
  String resource();

  /**
   * The ready.
   *
   * @return value. boolean
   */
  @JsonProperty("ready")
  Boolean ready();

  /**
   * The table version.
   *
   * @return value. string
   */
  @JsonProperty("tableVersion")
  String tableVersion();

}
