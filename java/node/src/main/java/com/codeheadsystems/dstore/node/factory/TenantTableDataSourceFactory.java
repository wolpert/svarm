package com.codeheadsystems.dstore.node.factory;

import com.codeheadsystems.dstore.node.engine.DatabaseConnectionEngine;
import com.codeheadsystems.dstore.node.engine.DatabaseInitializationEngine;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates data sources for tenantTables.
 */
@Singleton
public class TenantTableDataSourceFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantTableDataSourceFactory.class);

  private final DatabaseConnectionEngine databaseConnectionEngine;
  private final DatabaseInitializationEngine databaseInitializationEngine;

  /**
   * Constructor.
   *
   * @param databaseConnectionEngine     To get the URL data.
   * @param databaseInitializationEngine to initialize the tenant table.
   */
  @Inject
  public TenantTableDataSourceFactory(final DatabaseConnectionEngine databaseConnectionEngine,
                                      final DatabaseInitializationEngine databaseInitializationEngine) {
    LOGGER.info("TenantTableDataSourceFactory({},{})", databaseConnectionEngine, databaseInitializationEngine);
    this.databaseConnectionEngine = databaseConnectionEngine;
    this.databaseInitializationEngine = databaseInitializationEngine;
  }

  private static DataSource getComboPooledDataSource(final String url) {
    LOGGER.trace("getComboPooledDataSource() (If you are stuck here, Likely AES failure, your db and keys do not match)");
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(url);
    cpds.setUser("SA");
    cpds.setPassword("");
    cpds.setMinPoolSize(1);
    cpds.setAcquireIncrement(5);
    cpds.setMaxPoolSize(20);
    cpds.setMaxIdleTime(300);
    cpds.setTestConnectionOnCheckout(true);
    return cpds;
  }

  /**
   * Generate a new data source for the tenant table. This is not cached.
   *
   * @param tenantTable the tenant table to use.
   * @return the data source.
   */
  public DataSource generate(final TenantTable tenantTable) {
    LOGGER.debug("dataSource({})", tenantTable);
    final String url = databaseConnectionEngine.getTenantConnectionUrl(tenantTable);
    final DataSource dataSource = getComboPooledDataSource(url);
    try {
      LOGGER.trace("Getting connection");
      final Connection connection = dataSource.getConnection();
      databaseInitializationEngine.initialize(connection, tenantTable.tableVersion());
      return dataSource;
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to get tenant initialized connection", e);
    }
  }
}
