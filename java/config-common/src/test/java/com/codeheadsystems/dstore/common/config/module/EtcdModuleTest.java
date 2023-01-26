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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codeheadsystems.dstore.common.config.EtcdConfiguration;
import com.codeheadsystems.dstore.common.config.ImmutableEtcdConfiguration;
import com.codeheadsystems.dstore.common.engine.JsonEngine;
import com.codeheadsystems.dstore.common.factory.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

class EtcdModuleTest {

  private final JsonEngine jsonEngine = new JsonEngine(new ObjectMapperFactory().generate());

  @Test
  public void testFail() {
    final EtcdModule module = new EtcdModule(ImmutableEtcdConfiguration.builder().build());

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(module::client);
  }

  @Test
  public void target() {
    final String json = "{\"target\":\"ip:///etcd0:2379,etcd1:2379,etcd2:2379\"}";
    final EtcdModule module = new EtcdModule(jsonEngine.readValue(json, EtcdConfiguration.class));

    assertThat(module.client())
        .isNotNull();
  }

  @Test
  public void endpoints() {
    final String json = "{\"endpoints\":[\"http://etcd0:2379\", \"http://etcd1:2379\", \"http://etcd2:2379\"]}";
    final EtcdModule module = new EtcdModule(jsonEngine.readValue(json, EtcdConfiguration.class));

    assertThat(module.client())
        .isNotNull();
  }

}