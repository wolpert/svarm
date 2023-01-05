package com.codeheadsystems.dstore.node.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * The unique details for this instance of the node.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNodeInternalConfiguration.class)
@JsonDeserialize(builder = ImmutableNodeInternalConfiguration.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface NodeInternalConfiguration {

  /**
   * Our internal UUID, created on first run.
   *
   * @return uuid.
   */
  @JsonProperty("uuid")
  String uuid();

  /**
   * Our internal key, created on first run.
   *
   * @return the key base64 encoded.
   */
  @JsonProperty("key")
  String key();

  /**
   * Our nonce for the internal database.
   *
   * @return the nonce to use.
   */
  @JsonProperty("nonce")
  String nonce();

}
