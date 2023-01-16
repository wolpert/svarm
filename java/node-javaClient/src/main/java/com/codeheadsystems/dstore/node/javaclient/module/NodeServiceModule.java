package com.codeheadsystems.dstore.node.javaclient.module;

import com.codeheadsystems.dstore.node.api.NodeService;
import dagger.Module;
import dagger.Provides;
import feign.Feign;
import javax.inject.Singleton;

/**
 * Module for the node service.
 */
@Module
public class NodeServiceModule {

  private final String connectionUrl;

  /**
   * Constructor.
   *
   * @param connectionUrl used to connect to node.
   */
  public NodeServiceModule(final String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }

  /**
   * Get a usable node service.
   *
   * @param builder feign builder to use.
   * @return a node service.
   */
  @Provides
  @Singleton
  public NodeService nodeService(final Feign.Builder builder) {
    return builder.target(NodeService.class, connectionUrl);
  }
}
