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

package com.codeheadsystems.dstore.node.engine;

import java.sql.Connection;
import javax.inject.Inject;
import javax.inject.Singleton;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes the database using the proper liquibase files. Needed for provisioning a new datastore.
 */
@Singleton
public class DatabaseInitializationEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializationEngine.class);

  /**
   * Default constructor.
   */
  @Inject
  public DatabaseInitializationEngine() {
    LOGGER.info("DatabaseInitializationEngine()");
  }

  /**
   * Initializes the connection, which will use liquibase for common tables.
   *
   * @param connection to initialize.
   * @param path       where the liquibase files are.
   */
  public void initialize(final Connection connection,
                         final String path) {
    LOGGER.info("initialize({})", path); // INFO because this is rare
    try {
      Database database = DatabaseFactory.getInstance()
          .findCorrectDatabaseImplementation(new JdbcConnection(connection));
      Liquibase liquibase = new liquibase.Liquibase(
          "liquibase/" + path + "/liquibase-setup.xml",
          new ClassLoaderResourceAccessor(),
          database
      );
      liquibase.update(new Contexts(), new LabelExpression());
      LOGGER.info("complete");
    } catch (LiquibaseException e) {
      throw new IllegalStateException("Database update failure", e);
    }
  }

}
