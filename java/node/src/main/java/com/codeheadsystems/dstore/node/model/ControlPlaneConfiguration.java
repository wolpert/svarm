package com.codeheadsystems.dstore.node.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Used to get the configuration of the control plane.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableControlPlaneConfiguration.class)
@JsonDeserialize(builder = ImmutableControlPlaneConfiguration.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ControlPlaneConfiguration {

  /**
   * UUID for the control plane. (Overkill?)
   *
   * @return the uuid.
   */
  @JsonProperty("uuid")
  String uuid();

  /**
   * Key the control plane has for us.
   *
   * @return the key.
   */
  @JsonProperty("key")
  String key();

}
