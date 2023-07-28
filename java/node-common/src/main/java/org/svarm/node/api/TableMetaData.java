package org.svarm.node.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Metadata associated with the table.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTableMetaData.class)
@JsonDeserialize(builder = ImmutableTableMetaData.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TableMetaData {

  /**
   * What is the replication factor for the table.
   *
   * @return value. integer
   */
  Integer replicationFactor();

  /**
   * Table type.
   *
   * @return value. string
   */
  String type();

  /**
   * Types of tables.
   */
  enum Type {
    /**
     * Default type.
     */
    SINGLE_PRIMARY_KEY_V1
  }


}
