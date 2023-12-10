package org.svarm.node.engine.impl.v1singleentry;

import java.io.Serializable;
import org.immutables.value.Value;

/**
 * Represents a row in the v1 model.
 */
@Value.Immutable
public interface V1Row extends Serializable {

  /**
   * The id of the row.
   *
   * @return the value.
   */
  String id();

  /**
   * The column of the row.
   *
   * @return the value.
   */
  String cCol();

  /**
   * The hash of the row.
   *
   * @return the value.
   */
  Integer hash();

  /**
   * The timestamp of the row.
   *
   * @return the value.
   */
  Long timestamp();

  /**
   * The dataType of the row.
   *
   * @return the value.
   */
  String cDataType();

  /**
   * The data of the row.
   *
   * @return the value.
   */
  String cData();

}
