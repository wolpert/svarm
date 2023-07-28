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

package org.svarm.common.config.module;

import com.codahale.metrics.health.HealthCheck;
import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.etcd.jetcd.Client;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Named;
import javax.inject.Singleton;
import org.svarm.common.config.EtcdConfiguration;
import org.svarm.common.config.healthchecks.EtcdHealthCheck;

/**
 * This is not included automatically... you have to add it yourself if you want etcd support.
 */
@Module(includes = {EtcdModule.Binder.class})
public class EtcdModule {

  /**
   * External value to set if you want.
   */
  public static final String ETCD_ACCESSOR_PREAMBLE = "ETCD_ACCESSOR_PREAMBLE";

  /**
   * Internal value to set if you want.
   */
  public static final String INTERNAL_ETCD_ACCESSOR_PREAMBLE = "INTERNAL_ETCD_ACCESSOR_PREAMBLE";

  /**
   * Provide an executor service named here if you want to customize it.
   */
  public static final String WATCH_ENGINE_EXECUTOR = "WATCH_ENGINE_EXECUTOR";

  /**
   * Internal named executor service for the watch engine.
   */
  public static final String INTERNAL_WATCH_ENGINE_EXECUTOR = "INTERNAL_WATCH_ENGINE_EXECUTOR";

  /**
   * Provide the preamble for the accessor.
   *
   * @param preamble if we have a custom one.
   * @return our preamble.
   */
  @Provides
  @Singleton
  @Named(INTERNAL_ETCD_ACCESSOR_PREAMBLE)
  public String etcdPreamble(@Named(ETCD_ACCESSOR_PREAMBLE) final Optional<String> preamble) {
    return preamble.orElse("svarm");
  }

  /**
   * Provides the executor service.
   *
   * @param executorService if one is preconfigured.
   * @return the one to use.
   */
  @Provides
  @Singleton
  @Named(INTERNAL_WATCH_ENGINE_EXECUTOR)
  public ExecutorService executorService(@Named(WATCH_ENGINE_EXECUTOR) final Optional<ExecutorService> executorService) {
    return executorService.orElseGet(Executors::newSingleThreadExecutor);
  }

  /**
   * Provides the client.
   *
   * @param etcdConfiguration the configuration.
   * @return the client.
   */
  @Provides
  @Singleton
  public Client client(final EtcdConfiguration etcdConfiguration) {
    return etcdConfiguration.target()
        .map(s -> Client.builder().target(s))
        .orElseGet(() -> Client.builder().endpoints(etcdConfiguration.endpoints()
            .stream().map(URI::create).toArray(URI[]::new)))
        .build();
  }

  /**
   * Binds the health check.
   */
  @Module
  public interface Binder {

    /**
     * Binds the etcd health check.
     *
     * @param healthCheck to bind.
     * @return the health check.
     */
    @Binds
    @IntoSet
    HealthCheck etcHealthCheck(EtcdHealthCheck healthCheck);

    /**
     * Optional Preamble for the etcd configuration..
     *
     * @return value. string
     */
    @BindsOptionalOf
    @Named(ETCD_ACCESSOR_PREAMBLE)
    String etcdPreamble();

    /**
     * Optional ability for a client to set.
     *
     * @return executor service.
     */
    @BindsOptionalOf
    @Named(WATCH_ENGINE_EXECUTOR)
    ExecutorService executorService();
  }

}
