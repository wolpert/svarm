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

package com.codeheadsystems.dstore.node.engine;

import com.codeheadsystems.dstore.node.model.TenantTable;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * These methods isolate what is specific to the database itself. Any database specific
 * methods should originate here. This includes access via the data store.
 */
public interface DatabaseEngine {

  /**
   * Returns the data source for the tenant table. Not cached.
   *
   * @param table we want.
   * @return the data source.
   */
  DataSource tenantDataSource(final TenantTable table);

  /**
   * Returns the internal data source. Not cached.
   *
   * @return the data source.
   */
  DataSource internalDataSource();

  /**
   * Returns the location of the tenant table data store.
   *
   * @param tenantTable to look for.
   * @return the location.
   */
  default Optional<String> tenantDataStoreLocation(final TenantTable tenantTable) {
    return Optional.empty();
  }

}
