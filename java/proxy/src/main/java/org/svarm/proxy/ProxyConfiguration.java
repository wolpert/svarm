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

package org.svarm.proxy;

import org.svarm.server.ServerConfiguration;

/**
 * Configuration for the proxy.
 */
public class ProxyConfiguration extends ServerConfiguration {

  private int replicationFactor = 3;
  private int watchEngineThreads = 5;

  /**
   * Getter.
   *
   * @return value.
   */
  public int getReplicationFactor() {
    return replicationFactor;
  }

  /**
   * Setter.
   *
   * @param replicationFactor to set.
   */
  public void setReplicationFactor(final int replicationFactor) {
    this.replicationFactor = replicationFactor;
  }

  /**
   * Getter.
   *
   * @return value.
   */
  public int getWatchEngineThreads() {
    return watchEngineThreads;
  }

  /**
   * Setter.
   *
   * @param watchEngineThreads to set.
   */
  public void setWatchEngineThreads(final int watchEngineThreads) {
    this.watchEngineThreads = watchEngineThreads;
  }
}
