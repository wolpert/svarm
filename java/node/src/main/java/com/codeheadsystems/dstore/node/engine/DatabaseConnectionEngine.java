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

import static org.bouncycastle.util.encoders.Hex.toHexString;

import com.codeheadsystems.dstore.common.crypt.CryptUtils;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.manager.ControlPlaneManager;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles getting the connection URL with security enabled for hsqldb. It can do the
 * actual connection itself, but odds are this will move to a pooler.
 */
@Singleton
public class DatabaseConnectionEngine {

  /**
   * Identifier.
   */
  public static final String INTERNAL_DB_NAME = "nodeInternalDb";
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionEngine.class);
  private static final String CONNECTION_URL = "jdbc:hsqldb:file:%s/database;crypt_key=%s;crypt_iv=%s;crypt_type=AES/GCM-SIV/NoPadding;crypt_provider=BC;";

  private final ControlPlaneManager controlPlaneManager;
  private final NodeInternalConfiguration nodeInternalConfiguration;
  private final NodeConfiguration nodeConfiguration;
  private final CryptUtils cryptUtils;

  /**
   * Default constructor.
   *
   * @param controlPlaneManager       to use.
   * @param nodeInternalConfiguration to use.
   * @param nodeConfiguration         to use.
   * @param cryptUtils                to use.
   */
  @Inject
  public DatabaseConnectionEngine(final ControlPlaneManager controlPlaneManager,
                                  final NodeInternalConfiguration nodeInternalConfiguration,
                                  final NodeConfiguration nodeConfiguration,
                                  final CryptUtils cryptUtils) {
    LOGGER.info("DatabaseManager({})", nodeConfiguration);
    this.controlPlaneManager = controlPlaneManager;
    this.nodeInternalConfiguration = nodeInternalConfiguration;
    this.nodeConfiguration = nodeConfiguration;
    this.cryptUtils = cryptUtils;
    if (Security.getProvider("BC") == null) {
      LOGGER.info("Forcing Bouncy Castle provider into the list of available providers");
      Security.addProvider(new BouncyCastleProvider());
    } else {
      LOGGER.info("BC already a provider");
    }
  }

  /**
   * Creates a connection from the url. (Obsolete as we use datasources now)
   *
   * @param connectionUrl to use.
   * @return the connection.
   */
  public Connection connection(final String connectionUrl) {
    LOGGER.debug("connection()");
    try {
      return DriverManager.getConnection(connectionUrl, "SA", "");
    } catch (SQLException e) {
      throw new IllegalArgumentException("Unable to create/open database", e);
    }
  }

  /**
   * Gets the connection URL for the tenant.
   *
   * @param tenantId    to use.
   * @param tenantKey   to use with the one from the control plane.
   * @param tenantNonce to use, from us.
   * @return the URL.
   */
  public String getTenantConnectionUrl(final String tenantId,
                                       final String tenantKey,
                                       final String tenantNonce) {
    LOGGER.debug("getTenantConnectionUrl({})", tenantId);
    final String directory = getDatabasePath(tenantId);
    final byte[] key = cryptUtils.xor(tenantKey, controlPlaneManager.keyForTenant(tenantId));
    final byte[] nonce = cryptUtils.fromBase64(tenantNonce);
    return getConnectionUrl(directory, key, nonce);
  }

  /**
   * Generates the internal database to use.
   *
   * @return the URL.
   */
  public String getInternalConnectionUrl() {
    LOGGER.debug("getInternalConnectionUrl()");
    final String directory = getDatabasePath(INTERNAL_DB_NAME);
    final byte[] key = cryptUtils.xor(nodeInternalConfiguration.key(), controlPlaneManager.keyForNode());
    final byte[] nonce = cryptUtils.fromBase64(nodeInternalConfiguration.nonce());
    return getConnectionUrl(directory, key, nonce);
  }

  /**
   * Method to know if a database is already setup.
   *
   * @param name of the directory.
   * @return boolean if true or not.
   */
  public boolean isDatabaseSetup(final String name) {
    LOGGER.debug("isDatabaseSetup({})", name);
    final Path path = Path.of(nodeConfiguration.getDatabaseDirectory(), name);
    return Files.exists(path) && Files.isWritable(path) && Files.isDirectory(path);
  }

  private String getDatabasePath(final String name) {
    final Path directory = Path.of(nodeConfiguration.getDatabaseDirectory(), name);
    if (!Files.exists(directory)) {
      try {
        Files.createDirectories(directory);
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to create directory: " + directory);
      }
    }
    if (!Files.isDirectory(directory) && Files.isWritable(directory)) {
      throw new IllegalArgumentException("Not a writeable directory: " + directory);
    }
    return directory.toString();
  }

  /**
   * Gets a connection URL for HSQLDB, encrypted with AES/GCM/SIV
   *
   * @param databaseDirectory the location to use.
   * @param key               the key.
   * @param nonce             the nonce.
   * @return a URL ready to use.
   */
  public String getConnectionUrl(final String databaseDirectory,
                                 final byte[] key,
                                 final byte[] nonce) {
    LOGGER.debug("getConnectionUrl({})", databaseDirectory);
    // Details: http://hsqldb.org/doc/2.0/guide/management-chapt.html#mtc_encrypted_create
    return String.format(CONNECTION_URL, databaseDirectory, toHexString(key), toHexString(nonce));
  }

}
