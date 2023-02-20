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

package org.svarm.proxy.javaclient;

import dagger.Component;
import javax.inject.Singleton;
import org.svarm.common.javaclient.JavaClientModule;
import org.svarm.common.module.JsonModule;
import org.svarm.proxy.common.api.ProxyService;
import org.svarm.proxy.javaclient.module.ProxyServiceModule;

/**
 * Creates a NodeService java client.
 */
@Component(modules = {
    JavaClientModule.class,
    JsonModule.class,
    ProxyServiceModule.class
})
@Singleton
public interface ProxyServiceComponent {

  /**
   * Usable to create a fresh java client.
   *
   * @param connectionUrl to connect to.
   * @return a node service impl.
   */
  static ProxyServiceComponent controlNodeService(final String connectionUrl) {
    return DaggerProxyServiceComponent.builder()
        .proxyServiceModule(new ProxyServiceModule(connectionUrl))
        .build();
  }

  /**
   * The proxy service dagger will generate.
   *
   * @return a node service.
   */
  ProxyService proxyService();


}
