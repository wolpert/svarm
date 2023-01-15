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

import com.codeheadsystems.dstore.node.manager.DataSourceManager;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.metrics.Metrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Optional;
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
public class SqlEngine {

  /**
   * Identifier for metrics name.
   */
  public static final String SQLENGINE_EXECUTE = "SqlEngine.execute";

  /**
   * Identifier for metrics name.
   */
  public static final String SQLENGINE_EXECUTE_PREPARED = "SqlEngine.executePrepared";
  /**
   * Identifier for metrics name.
   */
  public static final String SQLENGINE_EXECUTE_CONNECTION = "SqlEngine.executeConnection";

  /**
   * Used to identify internal queries.
   */
  public static final String INTERNAL = "INTERNAL";
  private static final Logger LOGGER = LoggerFactory.getLogger(SqlEngine.class);

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
  public SqlEngine(final Metrics metrics, final DataSourceManager dataSourceManager) {
    LOGGER.info("SqlEngine({},{})", metrics, dataSourceManager);
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
    LOGGER.trace("executeQueryInternal({})", query);
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
    LOGGER.trace("executePreparedInternal({})", query);
    return executePrepared(INTERNAL,
        dataSourceManager.getInternalDataSource().orElseThrow(() -> new IllegalStateException("Database not setup")),
        query,
        function);
  }

  /**
   * Executes the prepared statement on the internal dataSource using metrics against the object itself.
   *
   * @param function that will process the prepared statement.
   * @param <R>      the type.
   * @return a instance of type-r.
   */
  public <R> R executeConnectionInternal(final Function<Connection, R> function) {
    LOGGER.trace("executeConnectionInternal()");
    return executeWithConnection(INTERNAL,
        dataSourceManager.getInternalDataSource().orElseThrow(() -> new IllegalStateException("Database not setup")),
        function);
  }

  /**
   * Executes the query on the tenant dataSource using metrics against the object itself.
   *
   * @param tenantTable tenant.
   * @param query       to execute.
   * @param function    that will process the result set.
   * @param <R>         the type.
   * @return a instance of type-r.
   */
  public <R> R executeQueryTenant(final TenantTable tenantTable,
                                  final String query,
                                  final Function<ResultSet, R> function) {
    LOGGER.trace("executeQueryTenant({})", query);
    return executeQuery(tenantTable.identifier().toString(),
        dataSourceManager.getDataSource(tenantTable),
        query,
        function);
  }

  /**
   * Executes the prepared statement on the tenant dataSource using metrics against the object itself.
   *
   * @param tenantTable tenant.
   * @param query       to execute.
   * @param function    that will process the prepared statement.
   * @param <R>         the type.
   * @return a instance of type-r.
   */
  public <R> R executePreparedTenant(final TenantTable tenantTable,
                                     final String query,
                                     final Function<PreparedStatement, R> function) {
    LOGGER.trace("executePreparedTenant({})", query);
    return executePrepared(tenantTable.identifier().toString(),
        dataSourceManager.getDataSource(tenantTable),
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
    LOGGER.trace("executeQuery({},{})", datasourceLookup, query);
    final Counter success =
        meterRegistry.counter(SQLENGINE_EXECUTE, "datasource", datasourceLookup, "success", "true");
    final Counter failure =
        meterRegistry.counter(SQLENGINE_EXECUTE, "datasource", datasourceLookup, "success", "false");
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
    LOGGER.trace("executePrepared({},{})", datasourceLookup, query);
    final Counter success =
        meterRegistry.counter(SQLENGINE_EXECUTE_PREPARED, "datasource", datasourceLookup, "success", "true");
    final Counter failure =
        meterRegistry.counter(SQLENGINE_EXECUTE_PREPARED, "datasource", datasourceLookup, "success", "false");
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

  /**
   * Executes the prepared using metrics against the object itself. You are required to close the result set.
   *
   * @param datasourceLookup to get the datasource to use.
   * @param dataSource       to execute against.
   * @param function         that will process the result set.
   * @param <R>              the type.
   * @return a instance of type-r.
   */
  private <R> R executeWithConnection(final String datasourceLookup,
                                      final DataSource dataSource,
                                      final Function<Connection, R> function) {
    LOGGER.trace("executeWithConnection({})", datasourceLookup);
    final Counter success =
        meterRegistry.counter(SQLENGINE_EXECUTE_CONNECTION, "datasource", datasourceLookup, "success", "true");
    final Counter failure =
        meterRegistry.counter(SQLENGINE_EXECUTE_CONNECTION, "datasource", datasourceLookup, "success", "false");
    final Timer timer = meterRegistry.timer(SQLENGINE_EXECUTE_CONNECTION, "datasource", datasourceLookup);
    return metrics.time(timer, success, failure, () -> {
      try (final Connection connection = dataSource.getConnection()) {
        return function.apply(connection);
      } catch (SQLException e) {
        throw new IllegalArgumentException("Unable to complete sql call", e);
      }
    });
  }

  /**
   * Helper method to manage nullable fields.
   *
   * @param colNumber         to set.
   * @param value             value.
   * @param preparedStatement preparedStatement.
   * @throws SQLException if we gots complains.
   */
  public void setStringField(final int colNumber,
                             final Optional<String> value,
                             final PreparedStatement preparedStatement) throws SQLException {
    if (value.isPresent()) {
      preparedStatement.setString(colNumber, value.get());
    } else {
      preparedStatement.setNull(colNumber, Types.VARCHAR);
    }
  }

  /**
   * Helper method to manage nullable fields.
   *
   * @param colNumber         to set.
   * @param value             value.
   * @param preparedStatement preparedStatement.
   * @throws SQLException if we gots complains.
   */
  public void setBigIntField(final int colNumber,
                             final Optional<Integer> value,
                             final PreparedStatement preparedStatement) throws SQLException {
    if (value.isPresent()) {
      preparedStatement.setInt(colNumber, value.get());
    } else {
      preparedStatement.setNull(colNumber, Types.BIGINT);
    }
  }
}
