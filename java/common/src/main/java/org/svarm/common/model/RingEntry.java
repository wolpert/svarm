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

package org.svarm.common.model;

import java.util.Set;
import org.immutables.value.Value;

/**
 * Provides for the location of the data in the ring.
 */
@Value.Immutable
public interface RingEntry {

  /**
   * The id itself.
   *
   * @return the id.
   */
  String id();

  /**
   * The hash of the id.
   *
   * @return the hash.
   */
  Integer hash();

  /**
   * Where the data is stored.
   *
   * @return the list of hashed locations.
   */
  Set<Integer> locationStores();

}