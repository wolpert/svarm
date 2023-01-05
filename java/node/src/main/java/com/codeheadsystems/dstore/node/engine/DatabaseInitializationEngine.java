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
    LOGGER.info("initialize({})", path);
    try {
      Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
      Liquibase liquibase = new liquibase.Liquibase(path + "/liquibase-setup.xml", new ClassLoaderResourceAccessor(), database);
      liquibase.update(new Contexts(), new LabelExpression());
      LOGGER.info("complete");
    } catch (LiquibaseException e) {
      throw new IllegalStateException("Database update failure", e);
    }
  }

}
