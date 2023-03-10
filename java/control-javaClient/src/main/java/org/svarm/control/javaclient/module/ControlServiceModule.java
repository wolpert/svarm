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

package org.svarm.control.javaclient.module;

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.retry.Retry;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.svarm.common.javaclient.FeignBuilderInstrumentator;
import org.svarm.control.common.api.ControlNodeService;
import org.svarm.control.common.api.ControlTenantResourceService;

/**
 * Module for the control service.
 */
@Module(includes = {ControlServiceModule.Binder.class})
public class ControlServiceModule {

  /**
   * Declare this in your module if you want to inject the configuration.
   */
  public static final String CONTROL_SERVICE_CONNECTION_URL = "ControlServiceConnectionUrl";
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
   * The decorators.
   *
   * @param retry to use.
   * @return the decorators.
   */
  @Provides
  @Singleton
  public FeignDecorators decorators(@Named("DEFAULT") final Retry retry) {
    return FeignDecorators.builder().withRetry(retry).build();
  }

  /**
   * Get a usable control service.
   *
   * @param decorators     to decorate.
   * @param instrumentator to get the feign builder.
   * @param url            the url of the control service.
   * @return a control service.
   */
  @Provides
  @Singleton
  public ControlNodeService nodeService(
      final FeignDecorators decorators,
      final FeignBuilderInstrumentator instrumentator,
      @Named(INTERNAL_CONTROL_SERVICE_CONNECTION_URL) final String url) {
    return instrumentator.generate(decorators).target(ControlNodeService.class, url);
  }

  /**
   * Get a usable control service.
   *
   * @param decorators     to decorate.
   * @param instrumentator to get the feign builder.
   * @param url            the url of the control service.
   * @return a control service.
   */
  @Provides
  @Singleton
  public ControlTenantResourceService controlTenantResourceService(
      final FeignDecorators decorators,
      final FeignBuilderInstrumentator instrumentator,
      @Named(INTERNAL_CONTROL_SERVICE_CONNECTION_URL) final String url) {
    return instrumentator.generate(decorators).target(ControlTenantResourceService.class, url);
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
