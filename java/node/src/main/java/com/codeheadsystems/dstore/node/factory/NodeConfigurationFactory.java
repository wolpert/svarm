package com.codeheadsystems.dstore.node.factory;

import com.codeheadsystems.dstore.common.crypt.CryptUtils;
import com.codeheadsystems.dstore.common.engine.JsonEngine;
import com.codeheadsystems.dstore.node.model.ImmutableNodeInternalConfiguration;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import java.io.File;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a unique internal node configuration for this instance. Specifically for a directory, but
 * the expectation is every node has its own configuration.
 */
@Singleton
public class NodeConfigurationFactory {

  /**
   * Identifier.
   */
  public static final String CONFIGURATION_NAME = "dataNodeConfig.json";

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeConfigurationFactory.class);

  private final CryptUtils cryptUtils;
  private final JsonEngine jsonEngine;

  /**
   * Default constructor.
   *
   * @param cryptUtils to use.
   * @param jsonEngine to use.
   */
  @Inject
  public NodeConfigurationFactory(final CryptUtils cryptUtils,
                                  final JsonEngine jsonEngine) {
    LOGGER.info("NodeConfigurationManager({},{})", cryptUtils, jsonEngine);
    this.jsonEngine = jsonEngine;
    this.cryptUtils = cryptUtils;
  }

  /**
   * Given the directory, will get the node internal configuration to use. If it does not exist,
   * this will generate the configuration since all values will be random.
   *
   * @param databaseDirectory that houses the configuration.
   * @return the configuration.
   */
  public NodeInternalConfiguration readOrGenerate(final String databaseDirectory) {
    LOGGER.info("readOrGenerate({})", databaseDirectory);
    final File directory = new File(databaseDirectory);
    if (!directory.exists() || !directory.isDirectory() || !directory.canWrite()) {
      throw new IllegalArgumentException("Cannot use " + databaseDirectory + " as a directory");
    }
    final File configureFile = new File(directory, CONFIGURATION_NAME);
    if (configureFile.exists()) {
      LOGGER.info("File found.");
      return jsonEngine.readValue(configureFile, NodeInternalConfiguration.class);
    } else {
      LOGGER.info("Generating.");
      final NodeInternalConfiguration configuration = ImmutableNodeInternalConfiguration.builder()
          .uuid(UUID.randomUUID().toString())
          .key(cryptUtils.toBase64(cryptUtils.randomKey(32)))
          .nonce(cryptUtils.toBase64(cryptUtils.randomKey(12)))
          .build();
      jsonEngine.writeValue(configureFile, configuration);
      return configuration;
    }
  }

}
