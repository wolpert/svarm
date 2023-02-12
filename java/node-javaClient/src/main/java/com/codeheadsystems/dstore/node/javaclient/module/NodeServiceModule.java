package com.codeheadsystems.dstore.node.javaclient.module;

import com.codeheadsystems.dstore.node.api.NodeTenantService;
import com.codeheadsystems.dstore.node.api.NodeTenantTableEntryService;
import com.codeheadsystems.dstore.node.api.NodeTenantTableService;
import dagger.Module;
import dagger.Provides;
import feign.Feign;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Module for the node service.
 */
@Module
public class NodeServiceModule {

  /**
   * Qualifier to differentiate from the control one.
   */
  public static final String NODE_NODE_TENANT_TABLE_SERVICE = "node.nodeTenantTableService";
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
  public NodeTenantService nodeTenantService(final Feign.Builder builder) {
    return builder.target(NodeTenantService.class, connectionUrl);
  }

  /**
   * Get a usable node service.
   *
   * @param builder feign builder to use.
   * @return a node service.
   */
  @Provides
  @Singleton
  @Named(NODE_NODE_TENANT_TABLE_SERVICE)
  public NodeTenantTableService nodeTenantTableService(final Feign.Builder builder) {
    return builder.target(NodeTenantTableService.class, connectionUrl);
  }

  /**
   * Get a usable node service.
   *
   * @param builder feign builder to use.
   * @return a node service.
   */
  @Provides
  @Singleton
  public NodeTenantTableEntryService nodeTenantTableEntryService(final Feign.Builder builder) {
    return builder.target(NodeTenantTableEntryService.class, connectionUrl);
  }
}
