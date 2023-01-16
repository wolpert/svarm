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

import static com.codeheadsystems.dstore.node.manager.DataSourceManager.TENANT;
import static com.codeheadsystems.dstore.node.module.DataSourceModule.INTERNAL;
import static org.assertj.core.api.Assertions.assertThat;

import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Stream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DatabaseInitializationEngineTest {

  private static final Random RANDOM = new Random();

  public static Stream<Arguments> pathToTableNames() {
    return Stream.of(
        Arguments.of("liquibasetest", new String[]{"DATABASECHANGELOGLOCK", "DATABASECHANGELOG", "PERSON", "OTHERTABLE"}),
        Arguments.of(INTERNAL, new String[]{"DATABASECHANGELOGLOCK", "DATABASECHANGELOG", "NODE_TENANT", "NODE_TENANT_TABLES", "NODE_TENANT_TABLES_CONFIGURATION"}),
        Arguments.of(TENANT, new String[]{"DATABASECHANGELOGLOCK", "DATABASECHANGELOG", "TENANT_DATA"})
    );
  }

  /**
   * Bytes for encryption.
   *
   * @param size of the bytes.
   * @return as a hex string.
   */
  static String bytes(final int size) {
    final byte[] bArray = new byte[size];
    RANDOM.nextBytes(bArray);
    return Hex.toHexString(bArray);
  }

  @BeforeEach
  void setupSecurity() {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @ParameterizedTest
  @MethodSource("pathToTableNames")
  void liquibaseTesting(final String path, final String... expectedTableNames) throws SQLException {
    final Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:DatabaseInitializationEngineTest-" +
            path + ";crypt_key=" + bytes(32) + ";crypt_iv=" + bytes(12) + ";crypt_type=AES/GCM-SIV/NoPadding;crypt_provider=BC;",
        "SA", "");
    new DatabaseInitializationEngine().initialize(connection, path);
    final HashSet<String> tableNames = new HashSet<>();
    try (ResultSet result = connection.createStatement().
        executeQuery("SELECT TABLE_NAME  FROM INFORMATION_SCHEMA.SYSTEM_TABLES where TABLE_SCHEM = 'PUBLIC'")) {
      while (result.next()) {
        tableNames.add(result.getString("TABLE_NAME"));
      }
    }
    assertThat(tableNames)
        .containsExactlyInAnyOrder(expectedTableNames);
  }

}