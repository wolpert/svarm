package com.codeheadsystems.dstore.proxy.javaclient.module;

import com.codeheadsystems.dstore.proxy.common.api.ProxyService;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import feign.Feign;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Module for the proxy service.
 */
@Module(includes = {ProxyServiceModule.Binder.class})
public class ProxyServiceModule {

  /**
   * Declare this in your module if you want to inject the configuration.
   */
  public static final String PROXY_SERVICE_CONNECTION_URL = "ProxyServiceConnectionUrl";
  private static final String INTERNAL_PROXY_SERVICE_CONNECTION_URL = "InternalProxyServiceConnectionUrl";
  private final String connectionUrl;


  /**
   * Default constructor. If you use this, you need to set the real connection value.
   */
  public ProxyServiceModule() {
    this(null);
  }

  /**
   * Constructor.
   *
   * @param connectionUrl used to connect to proxy.
   */
  public ProxyServiceModule(final String connectionUrl) {
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
  @Named(INTERNAL_PROXY_SERVICE_CONNECTION_URL)
  public String internalControlServiceConnectionUrl(
      @Named(PROXY_SERVICE_CONNECTION_URL) final Optional<String> url) {
    return url.orElse(connectionUrl);
  }

  /**
   * Get a usable proxy service.
   *
   * @param builder feign builder to use.
   * @param url     the url of the proxy service.
   * @return a proxy service.
   */
  @Provides
  @Singleton
  public ProxyService proxyService(
      final Feign.Builder builder,
      @Named(INTERNAL_PROXY_SERVICE_CONNECTION_URL) final String url) {
    return builder.target(ProxyService.class, url);
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
    @Named(PROXY_SERVICE_CONNECTION_URL)
    String proxyServiceConnectionUrl();
  }
}
