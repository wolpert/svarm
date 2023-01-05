package com.codeheadsystems.dstore.node;

import io.dropwizard.Configuration;

/**
 * Dropwizard configuration object.
 */
public class NodeConfiguration extends Configuration {

  String databaseDirectory;
  String controlPlaneUrl;

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
