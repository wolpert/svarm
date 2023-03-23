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

package org.svarm.proxy.module;

import static org.svarm.common.config.module.EtcdModule.WATCH_ENGINE_EXECUTOR;

import dagger.Module;
import dagger.Provides;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Named;
import javax.inject.Singleton;
import org.svarm.proxy.ProxyConfiguration;
import org.svarm.server.ServerConfiguration;

/**
 * For setting up the configuration for the service.
 */
@Module
public class ProxyModule {

  /**
   * Identifier for the node service executor.
   */
  public static final String NODE_SERVICE_EXECUTOR = "Node service executor";

  /**
   * Converts the configuration to a proxy configuration.
   *
   * @param configuration from dropwizard.
   * @return our type.
   */
  @Provides
  @Singleton
  public ProxyConfiguration configuration(final ServerConfiguration configuration) {
    return (ProxyConfiguration) configuration;
  }

  /**
   * Gets an executor service for the watch engine.
   *
   * @param proxyConfiguration to get the count from.
   * @return the service.
   */
  @Provides
  @Singleton
  @Named(WATCH_ENGINE_EXECUTOR)
  public ExecutorService executorService(final ProxyConfiguration proxyConfiguration) {
    return Executors.newFixedThreadPool(proxyConfiguration.getWatchEngineThreads());
  }

  /**
   * Gets an executor service for the node service engine.
   *
   * @param proxyConfiguration to get the count from.
   * @return the service.
   */
  @Provides
  @Singleton
  @Named(NODE_SERVICE_EXECUTOR)
  public ExecutorService nodeServiceExecutor(final ProxyConfiguration proxyConfiguration) {
    return Executors.newFixedThreadPool(proxyConfiguration.getNodeServiceThreads());
  }

}
