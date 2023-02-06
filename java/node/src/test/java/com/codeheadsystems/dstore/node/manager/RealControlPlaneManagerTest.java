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

package com.codeheadsystems.dstore.node.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.control.common.api.NodeInfo;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.accessor.ControlAccessor;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RealControlPlaneManagerTest extends BaseMetricTest {
  private static final String UUID = "uuid";
  private static final String TENANT = "tenant";
  private static final String KEY = "key";
  private static final String HOST = "host";
  private static final Integer PORT = 99999;

  @Mock private ControlAccessor controlAccessor;
  @Mock private NodeConfiguration nodeConfiguration;
  @Mock private NodeInternalConfiguration nodeInternalConfiguration;
  @Captor private ArgumentCaptor<String> stringArgumentCaptor;
  @Captor private ArgumentCaptor<Integer> integerArgumentCaptor;

  private RealControlPlaneManager manager;

  @BeforeEach
  void setup() {
    when(nodeConfiguration.getNodeHost()).thenReturn(HOST);
    when(nodeConfiguration.getNodePort()).thenReturn(PORT);
    when(nodeInternalConfiguration.uuid()).thenReturn(UUID);
    manager = new RealControlPlaneManager(metrics, controlAccessor, nodeInternalConfiguration, nodeConfiguration);
  }

  @Test
  void keyForTenant() {
    when(controlAccessor.keyForResource(UUID, TENANT)).thenReturn(KEY);
    assertThat(manager.keyForTenant(TENANT)).isEqualTo(KEY);
  }

  @Test
  void keyForNode_statusEnabled() {
    when(controlAccessor.status(UUID)).thenReturn(Optional.of(NodeInfo.Status.ENABLED.name()));
    when(controlAccessor.keyForNode(UUID)).thenReturn(KEY);
    assertThat(manager.keyForNode()).isEqualTo(KEY);
  }

  @Test
  void keyForNode_statusDisabled() {
    when(controlAccessor.status(UUID)).thenReturn(Optional.of(NodeInfo.Status.DISABLED.name())).thenReturn(Optional.of(NodeInfo.Status.ENABLED.name()));
    when(controlAccessor.keyForNode(UUID)).thenReturn(KEY);
    assertThat(manager.keyForNode()).isEqualTo(KEY);
    verify(controlAccessor).enable(stringArgumentCaptor.capture());
    assertThat(stringArgumentCaptor.getValue()).isEqualTo(UUID);
  }

  @Test
  void keyForNode_registered() {
    when(controlAccessor.status(UUID)).thenReturn(Optional.empty()).thenReturn(Optional.of(NodeInfo.Status.DISABLED.name())).thenReturn(Optional.of(NodeInfo.Status.ENABLED.name()));
    when(controlAccessor.keyForNode(UUID)).thenReturn(KEY);
    assertThat(manager.keyForNode()).isEqualTo(KEY);
    verify(controlAccessor).enable(stringArgumentCaptor.capture());
    verify(controlAccessor).register(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), integerArgumentCaptor.capture());
    assertThat(stringArgumentCaptor.getAllValues()).contains(UUID, HOST);
    assertThat(integerArgumentCaptor.getValue()).isEqualTo(PORT);
  }

  @Test
  void keyForNode_registeredFailed() {
    when(controlAccessor.status(UUID)).thenReturn(Optional.empty()).thenReturn(Optional.empty());
    assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> manager.keyForNode());
    verify(controlAccessor).register(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), integerArgumentCaptor.capture());
    assertThat(stringArgumentCaptor.getAllValues()).contains(UUID, HOST);
    assertThat(integerArgumentCaptor.getValue()).isEqualTo(PORT);
  }
}