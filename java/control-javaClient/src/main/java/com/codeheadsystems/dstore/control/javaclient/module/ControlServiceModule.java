package com.codeheadsystems.dstore.control.javaclient.module;

import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import com.codeheadsystems.dstore.node.api.NodeTenantTableService;
import dagger.Module;
import dagger.Provides;
import feign.Feign;
import javax.inject.Singleton;

/**
 * Module for the control service.
 */
@Module
public class ControlServiceModule {

  private final String connectionUrl;

  /**
   * Constructor.
   *
   * @param connectionUrl used to connect to control.
   */
  public ControlServiceModule(final String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }

  /**
   * Get a usable control service.
   *
   * @param builder feign builder to use.
   * @return a control service.
   */
  @Provides
  @Singleton
  public ControlNodeService nodeService(final Feign.Builder builder) {
    return builder.target(ControlNodeService.class, connectionUrl);
  }

  /**
   * Get a usable control service.
   *
   * @param builder feign builder to use.
   * @return a control service.
   */
  @Provides
  @Singleton
  public NodeTenantTableService nodeTenantTableService(final Feign.Builder builder) {
    return builder.target(NodeTenantTableService.class, connectionUrl);
  }
}
