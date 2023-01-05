package com.codeheadsystems.dstore.node.module;

import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.factory.NodeConfigurationFactory;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Provides access to the configuration itself.
 */
@Module
public class ConfigurationModule {

  private final NodeConfiguration nodeConfiguration;

  /**
   * Default constructor.
   *
   * @param configuration node configuration from dropwizard.
   */
  public ConfigurationModule(final NodeConfiguration configuration) {
    this.nodeConfiguration = configuration;
  }

  /**
   * Provide the configuration for others.
   *
   * @return node configuration.
   */
  @Provides
  @Singleton
  public NodeConfiguration nodeConfiguration() {
    return nodeConfiguration;
  }

  /**
   * Provides the internal node configuration for us.
   *
   * @param factory       a factory to build or read.
   * @param configuration startup configuration.
   * @return internal configuration.
   */
  @Provides
  @Singleton
  public NodeInternalConfiguration nodeInternalConfiguration(final NodeConfigurationFactory factory,
                                                             final NodeConfiguration configuration) {
    return factory.readOrGenerate(configuration.getDatabaseDirectory());
  }

}
