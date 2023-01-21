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

import com.codeheadsystems.dstore.control.model.Node;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;

/**
 * DAO for the Node.
 */
public interface NodeDao extends Transactional {

  /**
   * Inserts a Node by the values.
   *
   * @param instance to use.
   */
  @SqlUpdate("insert into NODES (uuid, create_date, update_date, host, port, verified) values (:uuid, :createDate, :updateDate, :host, :port, :verified)")
  void insert(@BindPojo final Node instance);

  /**
   * Inserts a Node by the values.
   *
   * @param instance to use.
   */
  @SqlUpdate("update NODES set create_date = :createDate, update_date = :updateDate, host = :host, port = :port, verified = :verified where uuid = :uuid")
  void update(@BindPojo final Node instance);

  /**
   * Get the key from the datastore.
   *
   * @param uuid to use.
   * @return the key.
   */
  @SqlQuery("select * from NODES where uuid = :uuid")
  Node read(@Bind("uuid") String uuid);

  /**
   * Delete the entry from the database.
   *
   * @param uuid to delete.
   */
  @SqlUpdate("delete from NODES where uuid = :uuid")
  void delete(@Bind("uuid") String uuid);

}