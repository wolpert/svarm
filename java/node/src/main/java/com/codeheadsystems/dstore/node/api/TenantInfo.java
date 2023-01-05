package com.codeheadsystems.dstore.node.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Details you can return back about the tenant to callers.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTenantInfo.class)
@JsonDeserialize(builder = ImmutableTenantInfo.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TenantInfo {

  /**
   * ID of the tenant.
   *
   * @return String.
   */
  @JsonProperty("id")
  String id();

}
