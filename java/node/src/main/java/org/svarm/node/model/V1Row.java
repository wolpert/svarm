package org.svarm.node.model;

import org.immutables.value.Value;

/**
 * Represents a row in the v1 model.
 */
@Value.Immutable
public interface V1Row {

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
  String column();

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
  String dataType();

  /**
   * The data of the row.
   *
   * @return the value.
   */
  String data();

}
