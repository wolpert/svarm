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

import io.dropwizard.Configuration;

/**
 * Dropwizard configuration object.
 */
public class NodeConfiguration extends Configuration {

  private String databaseDirectory;
  private String controlPlaneUrl;

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
}
