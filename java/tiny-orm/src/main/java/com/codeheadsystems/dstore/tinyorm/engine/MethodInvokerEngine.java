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

import com.codeheadsystems.dstore.tinyorm.model.ColumnDefinition;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Invokes methods hiding the exceptions.
 */
@Singleton
public class MethodInvokerEngine {

  /**
   * Default constructor.
   */
  @Inject
  public MethodInvokerEngine() {

  }

  /**
   * Returns the value from the get method.
   *
   * @param method   to call.
   * @param instance with this instance.
   * @param <T>      of this type.
   * @return the value.
   */
  @SuppressWarnings("unchecked")
  public <T> T get(final Method method, final Object instance) {
    try {
      return (T) method.invoke(instance);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Unable to access method", e);
    } catch (InvocationTargetException e) {
      throw new IllegalArgumentException("Unable to invoke method", e);
    }
  }

  /**
   * Sets the value in the prepared statement. Will figure out what value to set.
   *
   * @param instance we are working with. Has the value.
   * @param ps the prepared statement.
   * @param cd the column definition that has method to invoke.
   * @param colNumber number in the prepared statement to set.
   * @param <R> type of object.
   */
  public <R> void setPreparedStatement(final R instance, final PreparedStatement ps, final ColumnDefinition cd, final int colNumber) {
    switch (cd.returnType().getSimpleName()) {
      case "String":
        setString(instance, ps, cd, colNumber);
        break;
      case "Integer":
        setInteger(instance, ps, cd, colNumber);
        break;
      default:
        throw new IllegalArgumentException("We do not support yet: " + cd.returnType());
    }
  }

  private <R> void setString(final R instance, final PreparedStatement ps, final ColumnDefinition cd, final int colNumber) {
    try {
      final String value = get(cd.method(), instance);
      ps.setString(colNumber, value);
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to set string:" + cd, e);
    }
  }

  private <R> void setInteger(final R instance, final PreparedStatement ps, final ColumnDefinition cd, final int colNumber) {
    try {
      final Integer value = get(cd.method(), instance);
      ps.setInt(colNumber, value);
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to set integer:" + cd, e);
    }
  }
}
