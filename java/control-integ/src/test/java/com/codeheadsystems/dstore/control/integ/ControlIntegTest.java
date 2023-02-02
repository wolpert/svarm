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

package com.codeheadsystems.dstore.control.integ;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.dstore.common.factory.ObjectMapperFactory;
import com.codeheadsystems.dstore.control.Control;
import com.codeheadsystems.dstore.control.ControlConfiguration;
import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import com.codeheadsystems.dstore.control.common.api.ImmutableNodeMetaData;
import com.codeheadsystems.dstore.control.common.api.KeyInfo;
import com.codeheadsystems.dstore.control.common.api.NodeInfo;
import com.codeheadsystems.dstore.control.common.api.NodeMetaData;
import com.codeheadsystems.dstore.control.javaclient.ControlServiceComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.etcd.jetcd.test.EtcdClusterExtension;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("integ")
public class ControlIntegTest {
  private static DropwizardTestSupport<ControlConfiguration> SUPPORT;
  private static ObjectMapper OBJECT_MAPPER;
  private static Random RANDOM;
  private static String CONNECTION_URL;
  private static ControlNodeService CONTROL_NODE;

  @RegisterExtension
  public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
      .withNodes(1)
      .build();

  @BeforeAll
  static void setup() throws Exception {
    SUPPORT = new DropwizardTestSupport<>(
        Control.class,
        ResourceHelpers.resourceFilePath("control-integ-config.yaml")
        // TODO: put in the cluster endpoints into the config
    );
    SUPPORT.before();
    CONNECTION_URL = "http://localhost:" + SUPPORT.getLocalPort() + "/";
    CONTROL_NODE = ControlServiceComponent.controlNodeService(CONNECTION_URL).controlNodeService();
    OBJECT_MAPPER = new ObjectMapperFactory().generate();
    RANDOM = new Random();
  }

  @AfterAll
  static void tearDown() {
    SUPPORT.after();
  }

  @Test
  public void roundTrip() {
    final String uuid = UUID.randomUUID().toString();
    final NodeMetaData metaData = ImmutableNodeMetaData.builder().host("host").port(123).build();
    assertThat(CONTROL_NODE.register(uuid, metaData))
        .isNotNull()
        .hasFieldOrPropertyWithValue("uuid", uuid)
        .hasFieldOrPropertyWithValue("status", NodeInfo.Status.DISABLED.name());

    assertThat(CONTROL_NODE.enable(uuid))
        .isNotNull()
        .hasFieldOrPropertyWithValue("uuid", uuid)
        .hasFieldOrPropertyWithValue("status", NodeInfo.Status.ENABLED.name());

    final KeyInfo nodeKey = CONTROL_NODE.nodeKey(uuid);
    assertThat(nodeKey)
        .isNotNull()
        .extracting("key")
        .isNotNull();
    assertThat(CONTROL_NODE.nodeKey(uuid))
        .isNotNull()
        .extracting("key")
        .isEqualTo(nodeKey.key());

    final KeyInfo refKey = CONTROL_NODE.nodeKey(uuid, "reference");
    assertThat(refKey)
        .isNotNull()
        .extracting("key")
        .isNotNull();
    assertThat(CONTROL_NODE.nodeKey(uuid, "reference"))
        .isNotNull()
        .extracting("key")
        .isEqualTo(refKey.key());

    assertThat(CONTROL_NODE.disable(uuid))
        .isNotNull()
        .hasFieldOrPropertyWithValue("uuid", uuid)
        .hasFieldOrPropertyWithValue("status", NodeInfo.Status.DISABLED.name());

    assertThat(CONTROL_NODE.status(uuid))
        .isNotNull()
        .hasFieldOrPropertyWithValue("uuid", uuid)
        .hasFieldOrPropertyWithValue("status", NodeInfo.Status.DISABLED.name());
  }

}