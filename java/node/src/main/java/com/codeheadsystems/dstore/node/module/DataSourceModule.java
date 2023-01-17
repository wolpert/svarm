package com.codeheadsystems.dstore.node.module;

import com.codeheadsystems.dstore.node.engine.DatabaseConnectionEngine;
import com.codeheadsystems.dstore.node.engine.DatabaseInitializationEngine;
import com.codeheadsystems.dstore.node.model.Tenant;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import dagger.Module;
import dagger.Provides;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;

/**
 * Provides datasources.
 */
@Module
public class DataSourceModule {

  /**
   * Identifier for internal liquibase files.
   */
  public static final String INTERNAL = "internal";

  private static final int INTERNAL_MIN_POOL_SIZE = 1;

  private static DataSource getComboPooledDataSource(final int minPoolSize,
                                                     final String url) {
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(url);
    cpds.setUser("SA");
    cpds.setPassword("");
    cpds.setMinPoolSize(minPoolSize);
    cpds.setAcquireIncrement(5);
    cpds.setMaxPoolSize(20);
    cpds.setMaxIdleTime(300);
    cpds.setTestConnectionOnCheckout(true);
    return cpds;
  }

  /**
   * Returns a built, usable data source.
   *
   * @param databaseConnectionEngine     database connection engine.
   * @param databaseInitializationEngine database initialization engine.
   * @return the datasource.
   */
  @Provides
  @Singleton
  public DataSource internalDataSource(final DatabaseConnectionEngine databaseConnectionEngine,
                                       final DatabaseInitializationEngine databaseInitializationEngine) {
    final String url = databaseConnectionEngine.getInternalConnectionUrl();
    final DataSource dataSource = getComboPooledDataSource(INTERNAL_MIN_POOL_SIZE, url);
    final Connection connection;
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
    databaseInitializationEngine.initialize(connection, INTERNAL);
    return dataSource;
  }

  /**
   * The internal jdbi instance.
   *
   * @param dataSource to use.
   * @return a jdbi instance.
   */
  @Provides
  @Singleton
  public Jdbi internalJdbi(final DataSource dataSource) {
    final Jdbi jdbi = Jdbi.create(dataSource);
    jdbi.getConfig(JdbiImmutables.class).registerImmutable(Tenant.class);
    return jdbi;
  }
}
