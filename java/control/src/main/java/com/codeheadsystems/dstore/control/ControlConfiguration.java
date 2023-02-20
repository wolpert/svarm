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

package com.codeheadsystems.dstore.control;

import com.codeheadsystems.server.ServerConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Configuration for the control plane.
 */
public class ControlConfiguration extends ServerConfiguration {

  @Valid
  @NotNull
  private DataSourceFactory database = new DataSourceFactory();
  private Boolean runLiquibase;

  /**
   * Can be null. If set runs liquibase support.
   *
   * @return boolean, can be null.
   */
  public Boolean getRunLiquibase() {
    return runLiquibase;
  }

  /**
   * Include liquibase support.
   *
   * @param runLiquibase liquibase.
   */
  public void setRunLiquibase(final Boolean runLiquibase) {
    this.runLiquibase = runLiquibase;
  }

  /**
   * Getter for the factory.
   *
   * @return to get.
   */
  @JsonProperty("database")
  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  /**
   * Setter for the factory.
   *
   * @param factory to set.
   */
  @JsonProperty("database")
  public void setDataSourceFactory(DataSourceFactory factory) {
    this.database = factory;
  }

}
