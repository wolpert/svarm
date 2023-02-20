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

package org.svarm.common.engine;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Charsets;
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


  /**
   * Constructor.
   */
  @Inject
  public HashingEngine() {
    LOGGER.info("HashingEngine()");
  }

  /**
   * The main hashFunction for the system.
   *
   * @param value to be hashed.
   * @return the value.
   */
  public int murmur3(final String value) {
    return Hashing.murmur3_32_fixed().hashString(value, Charsets.UTF_8).asInt();
  }

  /**
   * Provides for hashing a string to the hex array.
   *
   * @param value to be hashed.
   * @return the string.
   */
  public String sha256(final String value) {
    return Hashing.sha256().hashString(value, Charsets.UTF_8).toString();
  }

}
