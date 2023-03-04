package org.svarm.datastore.common;

import dagger.MapKey;

/**
 * Table definitions.
 */
public enum TableDefinition {

  /**
   * Default single entry engine.
   */
  V1SingleEntryEngine;

  /**
   * Use this so components can make this a map for @IntoSet.
   */
  @MapKey
  public @interface TableDefinitionKey {

    /**
     * The value of the interface.
     *
     * @return the table definition.
     */
    TableDefinition value();
  }

}
