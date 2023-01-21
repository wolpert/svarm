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

package com.codeheadsystems.dstore.control.dao;

import com.codeheadsystems.dstore.control.model.Key;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * DAO for the Key.
 */
public interface KeyDao {

  /**
   * Inserts a key by the values. Need to make this via immutables one dya.
   *
   * @param key to use.
   */
  @SqlUpdate("insert into KEYS (id, create_date, key, nonce) values (:id, :createDate, :key, :nonce)")
  void insert(@BindPojo final Key key);

  /**
   * Get the key from the datastore.
   *
   * @param id to use.
   * @return the key.
   */
  @SqlQuery("select * from KEYS where id = :id")
  Key findKeyById(@Bind("id") String id);

}
