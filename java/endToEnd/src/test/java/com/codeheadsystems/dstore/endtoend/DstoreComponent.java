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

package com.codeheadsystems.dstore.endtoend;

import static com.codeheadsystems.dstore.control.javaclient.module.ControlServiceModule.CONTROL_NODE_TENANT_TABLE_SERVICE;
import static com.codeheadsystems.dstore.control.javaclient.module.ControlServiceModule.CONTROL_SERVICE_CONNECTION_URL;

import com.codeheadsystems.common.javaclient.JavaClientModule;
import com.codeheadsystems.dstore.common.config.EtcdConfiguration;
import com.codeheadsystems.dstore.common.config.ImmutableEtcdConfiguration;
import com.codeheadsystems.dstore.common.config.accessor.EtcdAccessor;
import com.codeheadsystems.dstore.common.config.module.EtcdModule;
import com.codeheadsystems.dstore.common.engine.TraceUuidEngine;
import com.codeheadsystems.dstore.common.module.JsonModule;
import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import com.codeheadsystems.dstore.control.javaclient.module.ControlServiceModule;
import com.codeheadsystems.dstore.node.api.NodeTenantTableService;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.etcd.jetcd.Client;
import javax.inject.Named;
import javax.inject.Singleton;

@Component(modules = {
    DstoreComponent.Configuration.class,
    EtcdModule.class,
    JavaClientModule.class,
    JsonModule.class,
    ControlServiceModule.class
})
@Singleton
public interface DstoreComponent {

  ControlNodeService controlNodeService();

  @Named(CONTROL_NODE_TENANT_TABLE_SERVICE)
  NodeTenantTableService nodeTenantTableService();

  Client client();

  TraceUuidEngine traceUuidEngine();
  EtcdAccessor etcdAccessor();

  @Module
  class Configuration {
    @Provides
    @Singleton
    public EtcdConfiguration etcdConfiguration() {
      return ImmutableEtcdConfiguration.builder().target("ip:///localhost:2379").build();
    }


    @Provides
    @Singleton
    @Named(CONTROL_SERVICE_CONNECTION_URL)
    String controlServiceConnectionUrl() {
      return "http://localhost:9090/";
    }
  }

}
