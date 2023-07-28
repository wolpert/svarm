package org.svarm.control.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * General meta data type of the resource.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TableMetaData.class, name = TableMetaData.TYPE),
})
public interface ResourceMetaData {

  /**
   * The type of object.
   *
   * @return value. string
   */
  @JsonProperty("type")
  String type();

}
