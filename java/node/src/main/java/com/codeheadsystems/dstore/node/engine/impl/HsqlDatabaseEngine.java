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

package com.codeheadsystems.dstore.node.engine.impl;

import static org.bouncycastle.util.encoders.Hex.toHexString;

import com.codeheadsystems.dstore.common.crypt.CryptUtils;
import com.codeheadsystems.dstore.common.engine.HashingEngine;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.engine.DatabaseEngine;
import com.codeheadsystems.dstore.node.manager.ControlPlaneManager;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.codeheadsystems.dstore.node.model.TenantTable;
import com.codeheadsystems.dstore.node.model.TenantTableIdentifier;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides for a Hsql implementation of the database engine, with encryption.
 */
@Singleton
public class HsqlDatabaseEngine implements DatabaseEngine {


  /**
   * Identifier.
   */
  public static final String INTERNAL_DB_NAME = "nodeInternalDb";
  private static final Logger LOGGER = LoggerFactory.getLogger(HsqlDatabaseEngine.class);

  // Details: http://hsqldb.org/doc/2.0/guide/management-chapt.html#mtc_encrypted_create
  private static final String CONNECTION_URL =
      "jdbc:hsqldb:file:%s/database;crypt_key=%s;crypt_iv=%s;crypt_type=AES/GCM-SIV/NoPadding;crypt_provider=BC;";
  private static final String IN_MEM_CONNECTION_URL =
      "jdbc:hsqldb:mem:%s/database;crypt_key=%s;crypt_iv=%s;crypt_type=AES/GCM-SIV/NoPadding;crypt_provider=BC;";
  private final ControlPlaneManager controlPlaneManager;
  private final NodeInternalConfiguration nodeInternalConfiguration;
  private final NodeConfiguration nodeConfiguration;
  private final CryptUtils cryptUtils;
  private final HashingEngine hashingEngine;
  private final String connectionUrlToUse;

  /**
   * Default constructor.
   *
   * @param controlPlaneManager       to use.
   * @param nodeInternalConfiguration to use.
   * @param nodeConfiguration         to use.
   * @param cryptUtils                to use.
   * @param hashingEngine             to use.
   */
  @Inject
  public HsqlDatabaseEngine(final ControlPlaneManager controlPlaneManager,
                            final NodeInternalConfiguration nodeInternalConfiguration,
                            final NodeConfiguration nodeConfiguration,
                            final CryptUtils cryptUtils, final HashingEngine hashingEngine) {
    this.hashingEngine = hashingEngine;
    LOGGER.info("DatabaseManager({},{},{},{})",
        controlPlaneManager, nodeInternalConfiguration, nodeConfiguration, cryptUtils);
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
    if (nodeConfiguration.isRunDatabaseInMemory()) {
      LOGGER.warn("\n---\n--- USING IN MEMORY DATABASE \n---");
      connectionUrlToUse = IN_MEM_CONNECTION_URL;
    } else {
      LOGGER.info("Using file based database");
      connectionUrlToUse = CONNECTION_URL;
    }
  }

  @Override
  public DataSource tenantDataSource(final TenantTable table) {
    LOGGER.trace("tenantDataSource({})", table);
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(getTenantConnectionUrl(table));
    cpds.setUser("SA");
    cpds.setPassword("");
    cpds.setMinPoolSize(0);
    cpds.setAcquireIncrement(10);
    cpds.setMaxPoolSize(40);
    cpds.setMaxIdleTime(300);
    //cpds.setTestConnectionOnCheckout(true);
    return cpds;
  }

  @Override
  public DataSource internalDataSource() {
    LOGGER.trace("internalDataSource()");
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(getInternalConnectionUrl());
    cpds.setUser("SA");
    cpds.setPassword("");
    cpds.setMinPoolSize(1);
    cpds.setAcquireIncrement(5);
    cpds.setMaxPoolSize(20);
    cpds.setMaxIdleTime(300);
    //cpds.setTestConnectionOnCheckout(true);
    return cpds;
  }

  /**
   * Generates the internal database to use.
   *
   * @return the URL.
   */
  private String getInternalConnectionUrl() {
    LOGGER.trace("getInternalConnectionUrl()");
    final String directory = getDatabasePath(INTERNAL_DB_NAME);
    final byte[] key = cryptUtils.xor(nodeInternalConfiguration.key(), controlPlaneManager.keyForNode());
    final byte[] nonce = cryptUtils.fromBase64(nodeInternalConfiguration.nonce());
    return getConnectionUrl(directory, key, nonce);
  }

  /**
   * Gets the connection URL for the tenant.
   *
   * @param tenantTable to use, from us.
   * @return the URL.
   */
  private String getTenantConnectionUrl(final TenantTable tenantTable) {
    LOGGER.trace("getTenantConnectionUrl({})", tenantTable);
    final String directory = getInternalTenantTableDirectoryStore(tenantTable);
    final byte[] key = cryptUtils.xor(tenantTable.key(),
        controlPlaneManager.keyForTenant(tenantTable.identifier().tenantId()));
    final byte[] nonce = cryptUtils.fromBase64(tenantTable.nonce());
    return getConnectionUrl(directory, key, nonce);
  }

  /**
   * Provides the TenantTable directory store.
   *
   * @param tenantTable we are looking for.
   * @return the location.
   */
  private String getInternalTenantTableDirectoryStore(final TenantTable tenantTable) {
    final String name = getTenantDatabaseName(tenantTable);
    return getDatabasePath(name);
  }

  @Override
  public Optional<String> tenantDataStoreLocation(final TenantTable tenantTable) {
    return Optional.of(getInternalTenantTableDirectoryStore(tenantTable));
  }

  private String getTenantDatabaseName(final TenantTable tenantTable) {
    final TenantTableIdentifier identifier = tenantTable.identifier();
    final String name = String.format("%s-%s", identifier.tenantId(), identifier.tableName());
    final String hexBytes = hashingEngine.sha256(name);
    LOGGER.info("Convert: from={} to={}", name, hexBytes);
    return hexBytes;
  }

  private String getDatabasePath(final String name) {
    final Path directory = Path.of(nodeConfiguration.getDatabaseDirectory(), name);
    LOGGER.info("getDatabasePath({})", directory);
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
   * Gets a connection URL for HSQLDB, encrypted with AES/GCM/SIV.
   *
   * @param databaseDirectory the location to use.
   * @param key               the key.
   * @param nonce             the nonce.
   * @return a URL ready to use.
   */
  private String getConnectionUrl(final String databaseDirectory,
                                  final byte[] key,
                                  final byte[] nonce) {
    LOGGER.trace("getConnectionUrl({})", databaseDirectory);
    return String.format(connectionUrlToUse, databaseDirectory, toHexString(key), toHexString(nonce));
  }
}
