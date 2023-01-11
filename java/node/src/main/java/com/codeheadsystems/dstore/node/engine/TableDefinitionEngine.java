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

package com.codeheadsystems.dstore.node.engine;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Methods needed for a table definition.
 */
public interface TableDefinitionEngine {

  /**
   * The hashing function needed for the table.
   *
   * @return a supplier for the required hashing function.
   */
  Supplier<HashFunction> hashFunctionSupplier();

  /**
   * Implementations need their own definition name for lookup.
   *
   * @return definition name.
   */
  String definitionName();

  /**
   * Hashes the string using the hashing function.
   *
   * @param string to hash.
   * @return the resulting hash.
   */
  default int hash(final String string) {
    return hashFunctionSupplier().get().hashUnencodedChars(string).asInt();
  }

  /**
   * First implementation.
   */
  @Singleton
  class V1SingleEntryEngine implements TableDefinitionEngine {

    /**
     * Identifier.
     */
    public static final String DEFINITION_NAME = "V1SingleEntryEngine";

    /**
     * Default constructor.
     */
    @Inject
    public V1SingleEntryEngine() {

    }

    /**
     * Uses the murmur3/32b hashing function.
     *
     * @return supplier.
     */
    @Override
    public Supplier<HashFunction> hashFunctionSupplier() {
      return Hashing::murmur3_32_fixed;
    }

    /**
     * Identifier.
     *
     * @return definition name.
     */
    @Override
    public String definitionName() {
      return DEFINITION_NAME;
    }
  }

}
