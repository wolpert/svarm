/*
 * Copyright (c) 2023. Ned Wolpert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.svarm.node.engine;

import java.util.Optional;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.model.TenantTable;

/**
 * Methods needed for a table definition.
 */
public interface TableDefinitionEngine {

  /**
   * Reads the entity from the table, returning the JsonNode.
   *
   * @param tenantTable table to read from.
   * @param entity      the entity id.
   * @return a EntryInfo if found.
   */
  Optional<EntryInfo> read(TenantTable tenantTable, String entity);

  /**
   * Writes the entity to the table.
   *
   * @param tenantTable table to write to.
   * @param entity      the entity.
   */
  void write(final TenantTable tenantTable, final EntryInfo entity);

  /**
   * Delete the entity from the table, returning the JsonNode.
   *
   * @param tenantTable table to delete from.
   * @param entity      the entity id.
   * @return boolean if anything was deleted.
   */
  boolean delete(TenantTable tenantTable, String entity);

  /**
   * Clear tombstones from the table.
   *
   * @param tenantTable table to delete from.
   * @return int number of tombstones cleared.
   */
  int clearTombstones(TenantTable tenantTable);
}
