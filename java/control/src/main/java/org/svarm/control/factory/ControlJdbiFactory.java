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

package org.svarm.control.factory;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.jdbi3.JdbiFactory;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.jdbi.v3.cache.caffeine.CaffeineCachePlugin;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.control.ControlConfiguration;
import org.svarm.control.model.Key;
import org.svarm.control.model.Node;
import org.svarm.control.model.NodeRange;

/**
 * Creates an instance of the JDBI object.
 */
@Singleton
public class ControlJdbiFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ControlJdbiFactory.class);

  /**
   * Default constructor.
   */
  @Inject
  public ControlJdbiFactory() {
    LOGGER.info("ControlJdbiFactory()");
  }

  /**
   * Default generator. Useful for tests and non-dropwizard stuff.
   *
   * @param url      to use.
   * @param username to use.
   * @param password to use.
   * @return configured Jdbi instance.
   */
  public Jdbi generate(final String url, final String username, final String password) {
    LOGGER.trace("generate({})", url);
    final Jdbi jdbi = Jdbi.create(url, username, password);
    setup(jdbi, true);
    return jdbi;
  }

  /**
   * Generate the dropwizard way.
   *
   * @param configuration with the details.
   * @param environment   the dropwizard environment.
   * @return jdbi.
   */
  public Jdbi generate(final ControlConfiguration configuration, Environment environment) {
    LOGGER.info("generate({},{})", configuration, environment);
    final JdbiFactory dbiFactory = new JdbiFactory();
    final Jdbi jdbi = dbiFactory.build(environment, configuration.getDataSourceFactory(), "database");
    setup(jdbi, configuration.getRunLiquibase());
    return jdbi;
  }

  private void setup(final Jdbi jdbi, final Boolean runLiquibase) {
    LOGGER.info("setup({},{})", jdbi, runLiquibase);
    jdbi.getConfig(JdbiImmutables.class)
        .registerImmutable(Key.class)
        .registerImmutable(Node.class)
        .registerImmutable(NodeRange.class)
        .registerImmutable(org.svarm.common.config.api.NodeRange.class);
    jdbi.installPlugin(new SqlObjectPlugin())
        .installPlugin(new CaffeineCachePlugin());
    if (Boolean.TRUE.equals(runLiquibase)) {
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

}
