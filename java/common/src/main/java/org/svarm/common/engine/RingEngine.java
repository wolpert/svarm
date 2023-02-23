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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.model.ImmutableRingEntry;
import org.svarm.common.model.RingEntry;

/**
 * Provides for a ring structure. Useful for the figuring out where things belong.
 */
@Singleton
public class RingEngine {

  /**
   * Provides the replication factory injection.
   */
  public static final String REPLICATION_FACTOR = "RingEngine.ReplicationFactor";
  private static final Logger LOGGER = getLogger(RingEngine.class);

  private final HashingEngine hashingEngine;
  private final int replicationFactor;
  private final long replicationAddition;

  /**
   * Constructor.
   *
   * @param replicationFactor number of times the data should be replicated.
   * @param hashingEngine     for hashing values.
   */
  @Inject
  public RingEngine(@Named(REPLICATION_FACTOR) final int replicationFactor,
                    final HashingEngine hashingEngine) {
    if (replicationFactor < 1) {
      throw new IllegalStateException("Cannot initialize with a replication factor < 1. Found " + replicationFactor);
    }
    this.replicationFactor = replicationFactor;
    this.hashingEngine = hashingEngine;
    this.replicationAddition = (((long) Integer.MAX_VALUE * 2L) - 1L) / (long) replicationFactor;
    LOGGER.info("RingEngine({},{},{})", hashingEngine, replicationFactor, replicationAddition);
  }

  /**
   * Calculates the ring entry for the id.
   *
   * @param id the id.
   * @return the ring entry.
   */
  public RingEntry ringEntry(final String id) {
    LOGGER.trace("ringEntry({})", id);
    final int hash = hashingEngine.murmur3(id);
    final Set<Integer> otherHashes = (replicationFactor > 1 ? getLocations(hash) : Set.of(hash));
    return ImmutableRingEntry.builder().id(id).hash(hash).locationStores(otherHashes).build();
  }

  private Set<Integer> getLocations(final int initialHash) {
    final ImmutableSet.Builder<Integer> builder = ImmutableSet.<Integer>builder().add(initialHash);
    int currentHash = initialHash;
    for (int i = 1; i < replicationFactor; i++) {
      long replicationHash = currentHash + replicationAddition;
      if (replicationHash > Integer.MAX_VALUE) {
        replicationHash -= Integer.MAX_VALUE; // remove the max value
        replicationHash += Integer.MIN_VALUE; // add what's left to the min value.
      }
      currentHash = Math.toIntExact(replicationHash);
      builder.add(currentHash);
    }
    return builder.build();
  }

}
