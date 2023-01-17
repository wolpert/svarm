package com.codeheadsystems.dstore.node.factory;

import com.codeheadsystems.dstore.node.engine.DatabaseConnectionEngine;
import com.codeheadsystems.dstore.node.engine.DatabaseInitializationEngine;
import com.codeheadsystems.dstore.node.model.TenantTable;
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
  private final DataSourceFactory dataSourceFactory;

  /**
   * Constructor.
   *
   * @param databaseConnectionEngine     To get the URL data.
   * @param databaseInitializationEngine to initialize the tenant table.
   * @param dataSourceFactory            to generate the ddtasource itself.
   */
  @Inject
  public TenantTableDataSourceFactory(final DatabaseConnectionEngine databaseConnectionEngine,
                                      final DatabaseInitializationEngine databaseInitializationEngine,
                                      final DataSourceFactory dataSourceFactory) {
    LOGGER.info("TenantTableDataSourceFactory({},{},{})",
        databaseConnectionEngine, databaseInitializationEngine, dataSourceFactory);
    this.databaseConnectionEngine = databaseConnectionEngine;
    this.databaseInitializationEngine = databaseInitializationEngine;
    this.dataSourceFactory = dataSourceFactory;
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
    final DataSource dataSource = dataSourceFactory.tenantDataSource(tenantTable, url);
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
