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

package org.svarm.control.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;
import org.svarm.control.model.Key;

/**
 * DAO for the Key.
 */
public interface KeyDao extends Transactional {

  /**
   * Inserts a key by the values.
   *
   * @param instance to use.
   */
  @SqlUpdate("insert into KEYS (id, create_date, key, nonce) values (:id, :createDate, :key, :nonce)")
  void insert(@BindPojo final Key instance);

  /**
   * Get the key from the datastore.
   *
   * @param id to use.
   * @return the key.
   */
  @SqlQuery("select * from KEYS where id = :id")
  Key read(@Bind("id") String id);

  /**
   * Delete the entry from the database.
   *
   * @param id to delete.
   */
  @SqlUpdate("delete from KEYS where id = :id")
  void delete(@Bind("id") String id);

}
