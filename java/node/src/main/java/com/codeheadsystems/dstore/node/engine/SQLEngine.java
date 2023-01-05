package com.codeheadsystems.dstore.node.engine;

import com.codeheadsystems.dstore.node.manager.DataSourceManager;
import com.codeheadsystems.metrics.Metrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides some ability to manage and work SQL queries.
 */
@Singleton
public class SQLEngine {

  /**
   * Identifier for metrics name.
   */
  public static final String SQLENGINE_EXECUTE = "SQLEngine.execute";

  /**
   * Identifier for metrics name.
   */
  public static final String SQLENGINE_EXECUTE_PREPARED = "SQLEngine.executePrepared";

  /**
   * Used to identify internal queries.
   */
  public static final String INTERNAL = "INTERNAL";
  private static final Logger LOGGER = LoggerFactory.getLogger(SQLEngine.class);

  private final Metrics metrics;
  private final MeterRegistry meterRegistry;
  private final DataSourceManager dataSourceManager;

  /**
   * Default constructor.
   *
   * @param metrics           object to use.
   * @param dataSourceManager manager for the ds.
   */
  @Inject
  public SQLEngine(final Metrics metrics, final DataSourceManager dataSourceManager) {
    LOGGER.info("SQLEngine({},{})", metrics, dataSourceManager);
    this.metrics = metrics;
    this.dataSourceManager = dataSourceManager;
    this.meterRegistry = metrics.registry();
  }

  /**
   * Executes the query on the internal dataSource using metrics against the object itself.
   *
   * @param query    to execute.
   * @param function that will process the result set.
   * @param <R>      the type.
   * @return a instance of type-r.
   */
  public <R> R executeQueryInternal(final String query,
                                    final Function<ResultSet, R> function) {
    return executeQuery(INTERNAL,
        dataSourceManager.getInternalDataSource().orElseThrow(() -> new IllegalStateException("Database not setup")),
        query,
        function);
  }

  /**
   * Executes the prepared statement on the internal dataSource using metrics against the object itself.
   *
   * @param query    to execute.
   * @param function that will process the prepared statement.
   * @param <R>      the type.
   * @return a instance of type-r.
   */
  public <R> R executePreparedInternal(final String query,
                                       final Function<PreparedStatement, R> function) {
    return executePrepared(INTERNAL,
        dataSourceManager.getInternalDataSource().orElseThrow(() -> new IllegalStateException("Database not setup")),
        query,
        function);
  }

  /**
   * Executes the query on the tenant dataSource using metrics against the object itself.
   *
   * @param tenantId tenant.
   * @param query    to execute.
   * @param function that will process the result set.
   * @param <R>      the type.
   * @return a instance of type-r.
   */
  public <R> R executeQueryTenant(final String tenantId,
                                  final String query,
                                  final Function<ResultSet, R> function) {
    return executeQuery(tenantId,
        dataSourceManager.getTenant(tenantId),
        query,
        function);
  }

  /**
   * Executes the prepared statement on the tenant dataSource using metrics against the object itself.
   *
   * @param tenantId tenant.
   * @param query    to execute.
   * @param function that will process the prepared statement.
   * @param <R>      the type.
   * @return a instance of type-r.
   */
  public <R> R executePreparedTenant(final String tenantId,
                                     final String query,
                                     final Function<PreparedStatement, R> function) {
    return executePrepared(tenantId,
        dataSourceManager.getTenant(tenantId),
        query,
        function);
  }

  /**
   * Executes the statement using metrics against the object itself.
   *
   * @param datasourceLookup to get the datasource to use.
   * @param dataSource       to execute against.
   * @param query            to execute.
   * @param function         that will process the result set.
   * @param <R>              the type.
   * @return a instance of type-r.
   */
  private <R> R executeQuery(final String datasourceLookup,
                             final DataSource dataSource,
                             final String query,
                             final Function<ResultSet, R> function) {
    LOGGER.debug("executeQuery({},{})", datasourceLookup, query);
    final Counter success = meterRegistry.counter(SQLENGINE_EXECUTE, "datasource", datasourceLookup, "success", "true");
    final Counter failure = meterRegistry.counter(SQLENGINE_EXECUTE, "datasource", datasourceLookup, "success", "false");
    final Timer timer = meterRegistry.timer(SQLENGINE_EXECUTE, "datasource", datasourceLookup);
    return metrics.time(timer, success, failure, () -> {
      try (final Connection connection = dataSource.getConnection()) {
        try (final Statement statement = connection.createStatement()) {
          try (final ResultSet resultSet = statement.executeQuery(query)) {
            return function.apply(resultSet);
          }
        }
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to complete sql call", e);
      }
    });
  }

  /**
   * Executes the prepared using metrics against the object itself. You are required to close the result set.
   *
   * @param datasourceLookup to get the datasource to use.
   * @param dataSource       to execute against.
   * @param query            to execute.
   * @param function         that will process the result set.
   * @param <R>              the type.
   * @return a instance of type-r.
   */
  private <R> R executePrepared(final String datasourceLookup,
                                final DataSource dataSource,
                                final String query,
                                final Function<PreparedStatement, R> function) {
    LOGGER.debug("executePrepared({},{})", datasourceLookup, query);
    final Counter success = meterRegistry.counter(SQLENGINE_EXECUTE_PREPARED, "datasource", datasourceLookup, "success", "true");
    final Counter failure = meterRegistry.counter(SQLENGINE_EXECUTE_PREPARED, "datasource", datasourceLookup, "success", "false");
    final Timer timer = meterRegistry.timer(SQLENGINE_EXECUTE_PREPARED, "datasource", datasourceLookup);
    return metrics.time(timer, success, failure, () -> {
      try (final Connection connection = dataSource.getConnection()) {
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
          return function.apply(statement);
        }
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to complete sql call", e);
      }
    });
  }
}
