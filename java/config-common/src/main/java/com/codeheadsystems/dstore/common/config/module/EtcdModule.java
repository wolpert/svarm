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

package com.codeheadsystems.dstore.common.config.module;

import com.codeheadsystems.dstore.common.config.EtcdConfiguration;
import dagger.Module;
import dagger.Provides;
import io.etcd.jetcd.Client;
import java.net.URI;
import javax.inject.Singleton;

/**
 * This is not included automatically... you have to add it yourself if you want etcd support.
 */
@Module
public class EtcdModule {

  private final EtcdConfiguration etcdConfiguration;

  /**
   * Constructor.
   *
   * @param etcdConfiguration from your dropwizard config.
   */
  public EtcdModule(final EtcdConfiguration etcdConfiguration) {
    this.etcdConfiguration = etcdConfiguration;
  }

  /**
   * Provides the client.
   *
   * @return the client.
   */
  @Provides
  @Singleton
  public Client client() {
    return etcdConfiguration.target()
        .map(s -> Client.builder().target(s))
        .orElseGet(() -> Client.builder().endpoints(etcdConfiguration.endpoints()
            .stream().map(URI::create).toArray(URI[]::new)))
        .build();
  }

}
