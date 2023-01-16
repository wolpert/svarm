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

package com.codeheadsystems.dstore.node.exception;

import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for exceptions.
 */
@Singleton
public class ExceptionUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionUtils.class);

  /**
   * Default constructor.
   */
  @Inject
  public ExceptionUtils() {

  }

  /**
   * Wraps the execution exception checking for it to really be 'not found'. Results is returns an
   * optional empty if not found.
   *
   * @param supplier that gets the value from the cache.
   * @param <T>      the type.
   * @return and optional of the type if it was found.
   */
  public <T> Optional<T> loadingCacheExecutionExceptionWrapper(final SupplierWithException<T> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (NotFoundException nfe) {
      LOGGER.trace("Not found (unchecked)");
      return Optional.empty();
    } catch (ExecutionException | UncheckedExecutionException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof NotFoundException) {
        LOGGER.trace("Not found (checked)");
        return Optional.empty();
      } else {
        LOGGER.error("Loading failed", cause);
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Provides for a supplier that tosses an execution exception.
   *
   * @param <T> type if supplies.
   */
  @FunctionalInterface
  public interface SupplierWithException<T> {

    /**
     * Supplier method.
     *
     * @return the type.
     * @throws ExecutionException if the loading of the cached failed.
     */
    T get() throws ExecutionException;
  }

}
