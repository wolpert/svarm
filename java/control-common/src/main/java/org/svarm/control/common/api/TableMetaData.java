package org.svarm.control.common.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.svarm.datastore.common.TableDefinition;

/**
 * Provides details on the table resource.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTableMetaData.class)
@JsonDeserialize(builder = ImmutableTableMetaData.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface TableMetaData extends ResourceMetaData {

  /**
   * Internal type for this class.
   */
  String TYPE = "table";

  @Value.Default
  @Override
  default String type() {
    return TYPE;
  }

  /**
   * The definition of the table.
   *
   * @return the value.
   */
  TableDefinition tableDefinition();

}
