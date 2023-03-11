package org.svarm.node.model;

import java.util.List;
import org.immutables.value.Value;

/**
 * Represent data store actions that can be taken.
 */
@Value.Immutable
public interface DataStoreActions<ADD, DELETE> {


  /**
   * Items to insert.
   *
   * @return the list.
   */
  List<ADD> insert();

  /**
   * Items to update.
   *
   * @return the list.
   */
  List<ADD> update();

  /**
   * Items to delete.
   *
   * @return the list.
   */
  List<DELETE> delete();

}
