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

package com.codeheadsystems.dstore.tinyorm.model;

import java.lang.reflect.Method;
import org.immutables.value.Value;

/**
 * A column definition for the tiny orm.
 */
@Value.Immutable
public interface ColumnDefinition extends Comparable<ColumnDefinition> {

  /**
   * Allows for comparisons.
   *
   * @param columnDefinition to compare.
   * @return comparison.
   */
  @Override
  default int compareTo(ColumnDefinition columnDefinition) {
    return columnName().compareTo(columnDefinition.columnName());
  }

  /**
   * Method with the annotation.
   *
   * @return the method.
   */
  Method method();

  /**
   * The column name in the database.
   *
   * @return the name.
   */
  @Value.NaturalOrder
  String columnName();

  /**
   * The return class type.
   *
   * @return return type.
   */
  Class<?> returnType();

}
