package com.codeheadsystems.dstore.control.javaclient.module;

import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import com.codeheadsystems.dstore.node.api.NodeTenantTableService;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import feign.Feign;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Module for the control service.
 */
@Module(includes = {ControlServiceModule.Binder.class})
public class ControlServiceModule {

  /**
   * Declare this in your module if you want to inject the configuration.
   */
  public static final String CONTROL_SERVICE_CONNECTION_URL = "ControlServiceConnectionUrl";
  /**
   * Qualifier for the node tenant table service to differ from the nodes one.
   */
  public static final String CONTROL_NODE_TENANT_TABLE_SERVICE = "control.nodeTenantTableService";
  private static final String INTERNAL_CONTROL_SERVICE_CONNECTION_URL = "InternalControlServiceConnectionUrl";
  private final String connectionUrl;


  /**
   * Default constructor. If you use this, you need to set the real connection value.
   */
  public ControlServiceModule() {
    this(null);
  }

  /**
   * Constructor.
   *
   * @param connectionUrl used to connect to control.
   */
  public ControlServiceModule(final String connectionUrl) {
    this.connectionUrl = connectionUrl;
  }

  /**
   * The real connection url.
   *
   * @param url the url.
   * @return the value.
   */
  @Provides
  @Singleton
  @Named(INTERNAL_CONTROL_SERVICE_CONNECTION_URL)
  public String internalControlServiceConnectionUrl(
      @Named(CONTROL_SERVICE_CONNECTION_URL) final Optional<String> url) {
    return url.orElse(connectionUrl);
  }

  /**
   * Get a usable control service.
   *
   * @param builder feign builder to use.
   * @param url     the url of the control service.
   * @return a control service.
   */
  @Provides
  @Singleton
  public ControlNodeService nodeService(final Feign.Builder builder,
                                        @Named(INTERNAL_CONTROL_SERVICE_CONNECTION_URL) final String url) {
    return builder.target(ControlNodeService.class, url);
  }

  /**
   * Get a usable control service.
   *
   * @param builder feign builder to use.
   * @param url     the url of the control service.
   * @return a control service.
   */
  @Provides
  @Singleton
  @Named(CONTROL_NODE_TENANT_TABLE_SERVICE)
  public NodeTenantTableService nodeTenantTableService(final Feign.Builder builder,
                                                       @Named(INTERNAL_CONTROL_SERVICE_CONNECTION_URL) final String url) {
    return builder.target(NodeTenantTableService.class, url);
  }

  /**
   * Binder so clients can do their own connection url.
   */
  @Module
  interface Binder {

    /**
     * Optional connection url declared by the clients.
     *
     * @return value.
     */
    @BindsOptionalOf
    @Named(CONTROL_SERVICE_CONNECTION_URL)
    String controlServiceConnectionUrl();
  }
}
