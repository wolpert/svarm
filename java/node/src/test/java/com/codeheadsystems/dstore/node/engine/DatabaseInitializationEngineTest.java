package com.codeheadsystems.dstore.node.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseInitializationEngineTest {

  private Connection connection;
  private DatabaseInitializationEngine engine;

  @BeforeEach
  void setUp() throws SQLException {
    connection = DriverManager.getConnection("jdbc:hsqldb:mem:DatabaseInitializationEngineTest", "SA", "");
    engine = new DatabaseInitializationEngine();
  }

  @Test
  void testLiquibase() throws SQLException {
    engine.initialize(connection, "liquibasetest");
    final HashSet<String> tableNames = new HashSet<>();
    try (ResultSet result = connection.createStatement().
        executeQuery("SELECT TABLE_NAME  FROM INFORMATION_SCHEMA.SYSTEM_TABLES where TABLE_SCHEM = 'PUBLIC'")) {
      while (result.next()) {
        tableNames.add(result.getString("TABLE_NAME"));
      }
    }
    assertThat(tableNames)
        .containsExactlyInAnyOrder("DATABASECHANGELOGLOCK", "DATABASECHANGELOG", "PERSON", "OTHERTABLE");
  }

}