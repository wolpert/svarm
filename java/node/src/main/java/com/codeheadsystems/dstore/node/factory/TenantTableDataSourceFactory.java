package com.codeheadsystems.dstore.node.factory;

import com.codeheadsystems.dstore.node.engine.DatabaseEngine;
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

  private final DatabaseEngine databaseEngine;
  private final DatabaseInitializationEngine databaseInitializationEngine;

  /**
   * Constructor.
   *
   * @param databaseEngine               To get the database datasource.
   * @param databaseInitializationEngine to initialize the tenant table.
   */
  @Inject
  public TenantTableDataSourceFactory(final DatabaseEngine databaseEngine,
                                      final DatabaseInitializationEngine databaseInitializationEngine) {
    LOGGER.info("TenantTableDataSourceFactory({},{})",
        databaseEngine, databaseInitializationEngine);
    this.databaseEngine = databaseEngine;
    this.databaseInitializationEngine = databaseInitializationEngine;
  }

  /**
   * Generate a new data source for the tenant table. This is not cached.
   *
   * @param tenantTable the tenant table to use.
   * @return the data source.
   */
  public DataSource generate(final TenantTable tenantTable) {
    LOGGER.debug("dataSource({})", tenantTable);
    final DataSource dataSource = databaseEngine.tenantDataSource(tenantTable);
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
