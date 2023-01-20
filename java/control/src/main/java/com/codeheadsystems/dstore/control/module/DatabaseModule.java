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

package com.codeheadsystems.dstore.control.module;

import com.codeheadsystems.dstore.control.ControlConfiguration;
import dagger.Module;
import dagger.Provides;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Singleton;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides management of the database. DAOs should be created here via the DBI.onDemand() method if possible.
 */
@Module
public class DatabaseModule {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseModule.class);

  private final Jdbi jdbi;

  /**
   * Constructor for the module.
   *
   * @param configuration from the configuration.
   * @param environment   from the runtime.
   */
  public DatabaseModule(final ControlConfiguration configuration, Environment environment) {
    LOGGER.info("DatabaseModule({},{})", configuration, environment);
    final JdbiFactory dbiFactory = new JdbiFactory();
    jdbi = dbiFactory.build(environment, configuration.getDataSourceFactory(), "database");
    if (Boolean.TRUE.equals(configuration.getRunLiquibase())) {
      jdbi.useHandle(this::runLiquibase);
    } else {
      LOGGER.info("runLiquibase(): false");
    }
  }

  private void runLiquibase(final Handle handle) {
    LOGGER.info("runLiquibase(): true");
    try (final Connection connection = handle.getConnection()) {
      final Database database = DatabaseFactory.getInstance()
          .findCorrectDatabaseImplementation(new JdbcConnection(connection));
      final Liquibase liquibase = new liquibase.Liquibase(
          "liquibase/liquibase-setup.xml",
          new ClassLoaderResourceAccessor(),
          database
      );
      liquibase.update(new Contexts(), new LabelExpression());
      LOGGER.info("runLiquibase(): complete");
    } catch (LiquibaseException | SQLException e) {
      throw new IllegalStateException("Database update failure", e);
    }
  }

  /**
   * The JDBI handler.
   *
   * @return dbi.
   */
  @Provides
  @Singleton
  public Jdbi jdbi() {
    return jdbi;
  }


}
