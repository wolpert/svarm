package com.codeheadsystems.dstore.node.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.node.manager.DataSourceManager;
import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SQLEngineTest extends BaseMetricTest {

  private static final String QUERY = "QUERY";
  @Mock private DataSourceManager dataSourceManager;

  @Mock private DataSource dataSource;
  @Mock private Connection connection;
  @Mock private Statement statement;
  @Mock private ResultSet resultSet;

  private AtomicInteger atomicInteger;
  private final Function<ResultSet, Integer> function = (rs) -> rs == resultSet ? atomicInteger.incrementAndGet() : atomicInteger.get();

  private SQLEngine sqlEngine;

  @BeforeEach
  void setup() {
    atomicInteger = new AtomicInteger(1);
    sqlEngine = new SQLEngine(metrics, dataSourceManager);
  }

  @Test
  void testExecuteQueryInternal() throws SQLException {
    when(dataSourceManager.getInternalDataSource()).thenReturn(Optional.of(dataSource));
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(statement.executeQuery(QUERY)).thenReturn(resultSet);

    final Integer result = sqlEngine.executeQueryInternal(QUERY, function);

    assertThat(result)
        .isNotNull()
        .isEqualTo(2);
  }

}