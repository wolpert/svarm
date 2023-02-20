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

package org.svarm.server;

import io.dropwizard.Configuration;
import org.svarm.common.config.EtcdConfiguration;

/**
 * A base server configuration for our stuff.
 */
public class ServerConfiguration extends Configuration {
  private EtcdConfiguration etcdConfiguration;
  private String stage = "dev"; // dev, test, alpha, beta, gamma, prod

  /**
   * Getter for the etcd configuration.
   *
   * @return value.
   */
  public EtcdConfiguration getEtcdConfiguration() {
    return etcdConfiguration;
  }

  /**
   * Setter for the etcd configuration.
   *
   * @param etcdConfiguration to set.
   */
  public void setEtcdConfiguration(final EtcdConfiguration etcdConfiguration) {
    this.etcdConfiguration = etcdConfiguration;
  }

  /**
   * The stage of deployment.
   *
   * @return value.
   */
  public String getStage() {
    return stage;
  }

  /**
   * The stage of deployment.
   *
   * @param stage value.
   */
  public void setStage(final String stage) {
    this.stage = stage;
  }
}
