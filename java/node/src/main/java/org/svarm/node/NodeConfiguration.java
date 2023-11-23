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

package org.svarm.node;

import org.svarm.server.ServerConfiguration;

/**
 * Dropwizard configuration object.
 */
public class NodeConfiguration extends ServerConfiguration {

  private String databaseDirectory;
  private String controlPlaneUrl;
  private boolean disableControlPlane = false;
  private boolean runDatabaseInMemory = false;
  private String nodeHost;
  private Integer nodePort = 8080;
  private int watchEngineThreads = 5;
  private String nodeScheme = "http";

  /**
   * Instantiates a new Node configuration.
   */
  public NodeConfiguration() {
    super();
  }

  /**
   * Gets the host that proxies can use to contact this node. Defaults to localhost.
   *
   * @return value. node host
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
   * @return value. node port
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
   * @return directory. database directory
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
   * @return url. control plane url
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
   * Returns if we have disables the control plane.
   *
   * @return value. boolean
   */
  public boolean isDisableControlPlane() {
    return disableControlPlane;
  }


  /**
   * Sets if we have disables the control plane.
   *
   * @param disableControlPlane value.
   */
  public void setDisableControlPlane(final boolean disableControlPlane) {
    this.disableControlPlane = disableControlPlane;
  }

  /**
   * Getter.
   *
   * @return value. watch engine threads
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

  /**
   * Gets node scheme.
   *
   * @return the node scheme
   */
  public String getNodeScheme() {
    return nodeScheme;
  }

  /**
   * Sets node scheme.
   *
   * @param nodeScheme the node scheme
   */
  public void setNodeScheme(final String nodeScheme) {
    this.nodeScheme = nodeScheme;
  }
}
