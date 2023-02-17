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

package com.codeheadsystems.dstore.common.engine;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Provides generic hashing functions.
 */
@Singleton
public class HashingEngine {

  private static final Logger LOGGER = getLogger(HashingEngine.class);

  private final ThreadLocal<HashFunction> hasherFunction;

  /**
   * Constructor.
   */
  @Inject
  public HashingEngine() {
    hasherFunction = ThreadLocal.withInitial(Hashing::murmur3_32_fixed);
    LOGGER.info("HashingEngine(): murmur3_32_fixed");
  }

  /**
   * The main hashFunction for the system.
   *
   * @param value to be hashed.
   * @return the value.
   */
  public int hash(final String value) {
    return hasherFunction.get().hashUnencodedChars(value).asInt();
  }

}
