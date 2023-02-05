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

package com.codeheadsystems.dstore.node;

import com.codeheadsystems.dstore.common.config.EtcdConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

/**
 * Dropwizard configuration object.
 */
public class NodeConfiguration extends Configuration {

  private String databaseDirectory;
  private String controlPlaneUrl;
  private EtcdConfiguration etcdConfiguration;
  private boolean runDatabaseInMemory = false;
  private String nodeHost = "localhost";
  private Integer nodePort = 8080;

  /**
   * Gets the host that proxies can use to contact this node. Defaults to localhost.
   *
   * @return value.
   */
  public String getNodeHost() {
    return nodeHost;
  }

  /**
   * Sets the host that proxies can use to contact this node. Defaults to localhost.
   *
   * @param nodeHost value.
   */
  public void setNodeHost(final String nodeHost) {
    this.nodeHost = nodeHost;
  }

  /**
   * Gets the port that proxies can use to contact this node. Defaults to 8080.
   *
   * @return value.
   */
  public Integer getNodePort() {
    return nodePort;
  }


  /**
   * Sets the port that proxies can use to contact this node. Defaults to 8080.
   *
   * @param nodePort value.
   */
  public void setNodePort(final Integer nodePort) {
    this.nodePort = nodePort;
  }

  /**
   * Accessor to the boolean.
   *
   * @return the boolean.
   */
  public boolean isRunDatabaseInMemory() {
    return runDatabaseInMemory;
  }

  /**
   * Sets the database run in memory.
   *
   * @param runDatabaseInMemory to set.
   */
  public void setRunDatabaseInMemory(final boolean runDatabaseInMemory) {
    this.runDatabaseInMemory = runDatabaseInMemory;
  }

  /**
   * Getter for database directory.
   *
   * @return directory.
   */
  public String getDatabaseDirectory() {
    return databaseDirectory;
  }

  /**
   * Setter for database directory.
   *
   * @param databaseDirectory directory.
   */
  public void setDatabaseDirectory(final String databaseDirectory) {
    this.databaseDirectory = databaseDirectory;
  }

  /**
   * Getter for the control plane url.
   *
   * @return url.
   */
  public String getControlPlaneUrl() {
    return controlPlaneUrl;
  }

  /**
   * Setter for the control plane url.
   *
   * @param controlPlaneUrl the url.
   */
  public void setControlPlaneUrl(final String controlPlaneUrl) {
    this.controlPlaneUrl = controlPlaneUrl;
  }

  /**
   * The ETCD Configuration.
   *
   * @return the configuration.
   */
  @JsonProperty("etcdConfiguration")
  public EtcdConfiguration getEtcdConfiguration() {
    return etcdConfiguration;
  }

  /**
   * The etc configuration.
   *
   * @param etcdConfiguration to be set.
   */
  public void setEtcdConfiguration(final EtcdConfiguration etcdConfiguration) {
    this.etcdConfiguration = etcdConfiguration;
  }
}
