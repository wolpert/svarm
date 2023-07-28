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

package org.svarm.proxy.javaclient.module;

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.retry.Retry;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.svarm.common.javaclient.FeignBuilderInstrumentator;
import org.svarm.proxy.common.api.ProxyService;

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
   * @param instrumentator feign builder to use.
   * @param retry          default retry policy.
   * @param url            the url of the proxy service.
   * @return a proxy service.
   */
  @Provides
  @Singleton
  public ProxyService proxyService(
      final FeignBuilderInstrumentator instrumentator,
      @Named("DEFAULT") final Retry retry,
      @Named(INTERNAL_PROXY_SERVICE_CONNECTION_URL) final String url) {
    final FeignDecorators feignDecorators = FeignDecorators.builder().withRetry(retry).build();
    return instrumentator.generate(feignDecorators).target(ProxyService.class, url);
  }

  /**
   * Binder so clients can do their own connection url.
   */
  @Module
  interface Binder {

    /**
     * Optional connection url declared by the clients.
     *
     * @return value. string
     */
    @BindsOptionalOf
    @Named(PROXY_SERVICE_CONNECTION_URL)
    String proxyServiceConnectionUrl();
  }
}
