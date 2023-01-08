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

package com.codeheadsystems.dstore.tinyorm.engine;

import com.codeheadsystems.dstore.tinyorm.factory.ColumnDefinitionFactory;
import com.codeheadsystems.dstore.tinyorm.model.ColumnDefinition;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A naive tiny orm engine.
 */
@Singleton
public class TinyOrmEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(TinyOrmEngine.class);

  private final ColumnDefinitionFactory columnDefinitionFactory;
  private final MethodInvokerEngine methodInvokerEngine;

  /**
   * Constructor.
   *
   * @param columnDefinitionFactory to use.
   * @param methodInvokerEngine     to use.
   */
  @Inject
  public TinyOrmEngine(final ColumnDefinitionFactory columnDefinitionFactory,
                       final MethodInvokerEngine methodInvokerEngine) {
    LOGGER.info("TinyOrmEngine({},{})", columnDefinitionFactory, methodInvokerEngine);
    this.columnDefinitionFactory = columnDefinitionFactory;
    this.methodInvokerEngine = methodInvokerEngine;
  }

  /**
   * Provides the insert query to create rows in the table. We require the table name because there could be
   * multiple tables for the same type of object here. The can be executed one and reused all the time.
   *
   * @param modelClass class that we modeled.
   * @param tableName  we want to use.
   * @return the string.
   */
  public String insertQuery(final Class<?> modelClass,
                            final String tableName) {
    LOGGER.debug("insertQuery({},{}+", modelClass, tableName);
    final List<String> columns = columnDefinitionFactory.columnDefinitions(modelClass).stream()
        .map(ColumnDefinition::columnName)
        .toList();
    return new StringBuilder("insert into ").append(tableName)
        .append(" (").append(String.join(",", columns))
        .append(") values (").append(columns.stream().map(x -> "?").collect(Collectors.joining(",")))
        .append(")").toString();
  }

  /**
   * Clousre to insert an objcet into the table.
   *
   * @param instance of the object.
   * @param <R>      type of object.
   * @return the clouser.
   */
  public <R> Function<PreparedStatement, R> insertPreparedStatement(R instance) {
    return (ps) -> {
      final AtomicInteger colNumberHolder = new AtomicInteger(0);
      columnDefinitionFactory.columnDefinitions(instance.getClass())
          .forEach(cd -> methodInvokerEngine.setPreparedStatement(instance, ps, cd, colNumberHolder.incrementAndGet()));
      try {
        ps.execute();
        if (ps.getUpdateCount() != 1) {
          throw new IllegalArgumentException("Unable to create tenant");
        }
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to insert new data", e);
      }
      return instance;
    };
  }

}

