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

 /**
  * - RID_ID: Indexed, first part of the primary composite key. This is the unique identifier
  * - C_COL:  Indexed, second part of the primary composite key.
  * - HASH: The hash value of the RID_ID for mgmt.
  * - C_DATA_TYPE: Enum, either String or Integer.
  * - C_DATA: The actual data for that table columm.
  */

create table CUSTOMER_PROVIDED_TABLE_NAME (
    RID_ID varchar(256) NOT NULL,
    C_COL varchar(256) NOT NULL,
    HASH varchar(256) NOT NULL,
    C_DATA_TYPE varchar(256) NOT NULL,
    C_DATA varchar(4096) NOT NULL,
    PRIMARY KEY (RID_ID, C_COL)
);

/**
  * This is not a unique index.
 */
create index CUSTOMER_PROVIDED_TABLE_NAME_HASH_IDX on CUSTOMER_PROVIDED_TABLE_NAME(HASH);