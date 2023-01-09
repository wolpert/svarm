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

package com.codeheadsystems.dstore.node.model;

import org.immutables.value.Value;

/**
 * RID_ID: Indexed, first part of the primary composite key. This is the unique identifier
 * C_COL: Indexed, second part of the primary composite key.
 * HASH: The hash value of the RID_ID for mgmt.
 * C_DATA_TYPE: Enum, either String or Integer.
 * C_DATA: Nullable String.
 */
@Value.Immutable
public interface TenantTableEntry {

  /**
   * The id of the row/entry.
   *
   * @return String.
   */
  String id();

  /**
   * The name of the row column.
   *
   * @return String.
   */
  String column();

  /**
   * The hash value for the entry. (Based on the id). This is indexed too.
   *
   * @return String.
   */
  @Value.Auxiliary
  String hash();

  /**
   * The type of data. Only Integer and String supported.
   *
   * @return String.
   */
  @Value.Auxiliary
  String dataType();

  /**
   * The data itself.
   *
   * @return String.
   */
  @Value.Auxiliary
  String data();

}
