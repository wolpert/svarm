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

package com.codeheadsystems.dstore.tinyorm.factory;

import com.codeheadsystems.dstore.tinyorm.annotation.ColName;
import com.codeheadsystems.dstore.tinyorm.model.ColumnDefinition;
import com.codeheadsystems.dstore.tinyorm.model.ImmutableColumnDefinition;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory to generate column definitions.
 */
@Singleton
public class ColumnDefinitionFactory {

  /**
   * Default constructor.
   */
  @Inject
  public ColumnDefinitionFactory() {

  }

  /**
   * Returns a sorted list of column definitions for the class, if any.
   *
   * @param clazz class to check.
   * @return the list.
   */
  public List<ColumnDefinition> columnDefinitions(final Class<?> clazz) {
    final Class<?> toUse = (clazz.getSimpleName().startsWith("Immutable") ? clazz.getInterfaces()[0] : clazz);
    return Arrays.stream(toUse.getDeclaredMethods())
        .map(this::columnDefinition)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .sorted()
        .toList();
  }

  /**
   * Generate a single column definition for the method. Is nullable if there is no annotation.
   *
   * @param method to build.
   * @return the definition if set.
   */
  public Optional<ColumnDefinition> columnDefinition(final Method method) {
    final ColName colName = method.getDeclaredAnnotation(ColName.class);
    if (colName == null) {
      return Optional.empty();
    } else {
      final ColumnDefinition columnDefinition = ImmutableColumnDefinition.builder()
          .method(method)
          .columnName(colName.value())
          .returnType(method.getReturnType())
          .build();
      return Optional.of(columnDefinition);
    }
  }

}
