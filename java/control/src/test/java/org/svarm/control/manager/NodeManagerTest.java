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

package org.svarm.control.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.time.Clock;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.control.common.api.NodeInfo;
import org.svarm.control.common.api.NodeMetaData;
import org.svarm.control.dao.NodeDao;
import org.svarm.control.model.ImmutableNode;
import org.svarm.control.model.Key;
import org.svarm.control.model.Node;
import org.svarm.server.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class NodeManagerTest extends BaseMetricTest {

  private static final String UUID = "uuid";
  private static final String STATUS = "status";
  private static final String URI = "uri";
  private static final String TENANT = "Tenant";
  @Mock private NodeDao nodeDao;
  @Mock private KeyManager keyManager;
  @Mock private Clock clock;
  @Mock private NodeMetaData nodeMetaData;
  @Mock private Node node;
  @Mock private Key key;

  @Captor private ArgumentCaptor<Node> nodeArgumentCaptor;

  private NodeManager nodeManager;

  @BeforeEach
  void setup() {
    nodeManager = new NodeManager(nodeDao, keyManager, clock, metrics);
  }

  @Test
  public void status_notFound() {
    assertThat(nodeManager.status(UUID))
        .isNotPresent();
  }

  @Test
  public void status_found() {
    when(node.status()).thenReturn(STATUS);
    when(nodeDao.read(UUID)).thenReturn(node);
    assertThat(nodeManager.status(UUID))
        .isPresent()
        .contains(STATUS);
  }

  @Test
  public void create_found() {
    when(nodeDao.read(UUID)).thenReturn(node);
    final Node result = nodeManager.create(UUID, nodeMetaData);
    assertThat(result).isEqualTo(node);
  }

  @Test
  public void create() {
    when(nodeMetaData.uri()).thenReturn(URI);
    when(clock.instant()).thenReturn(Instant.now());
    final Node result = nodeManager.create(UUID, nodeMetaData);
    assertThat(result.status()).isEqualTo(NodeInfo.Status.DISABLED.name());
    verify(nodeDao).insert(nodeArgumentCaptor.capture());
    assertThat(nodeArgumentCaptor.getValue())
        .isEqualTo(result)
        .hasFieldOrPropertyWithValue("uuid", UUID)
        .hasFieldOrPropertyWithValue("uri", URI);
  }

  @Test
  public void key_node_found() {
    when(node.status()).thenReturn(NodeInfo.Status.ENABLED.name());
    when(nodeDao.read(UUID)).thenReturn(node);
    when(keyManager.getNodeKey(UUID)).thenReturn(key);
    Assertions.assertThat(nodeManager.key(UUID)).isEqualTo(key);
  }

  @Test
  public void key_node_disabled() {
    when(node.status()).thenReturn(NodeInfo.Status.DISABLED.name());
    when(nodeDao.read(UUID)).thenReturn(node);
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> nodeManager.key(UUID));
  }

  @Test
  public void key_node_notFound() {
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> nodeManager.key(UUID));
  }

  @Test
  public void key_nodeTenant_found() {
    when(node.status()).thenReturn(NodeInfo.Status.ENABLED.name());
    when(nodeDao.read(UUID)).thenReturn(node);
    when(keyManager.getNodeKey(UUID, "tenant:" + TENANT)).thenReturn(key);
    Assertions.assertThat(nodeManager.key(UUID, TENANT)).isEqualTo(key);
  }

  @Test
  public void key_nodeTenant_disabled() {
    when(node.status()).thenReturn(NodeInfo.Status.DISABLED.name());
    when(nodeDao.read(UUID)).thenReturn(node);
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> nodeManager.key(UUID, TENANT));
  }

  @Test
  public void key_nodeTenant_notFound() {
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> nodeManager.key(UUID, TENANT));
  }

  @Test
  public void enable_found_currentDisabled() {
    when(nodeDao.read(UUID)).thenReturn(generate(NodeInfo.Status.DISABLED.name()));
    when(clock.instant()).thenReturn(Instant.ofEpochMilli(System.currentTimeMillis()));
    nodeManager.enable(UUID);
    verify(nodeDao).update(nodeArgumentCaptor.capture());
    assertThat(nodeArgumentCaptor.getValue())
        .isNotNull()
        .hasFieldOrPropertyWithValue("status", NodeInfo.Status.ENABLED.name())
        .extracting("updateDate")
        .isNotNull();
  }

  @Test
  public void enable_found_currentEnabled() {
    when(nodeDao.read(UUID)).thenReturn(generate(NodeInfo.Status.ENABLED.name()));
    nodeManager.enable(UUID);
    verify(nodeDao, times(0)).update(nodeArgumentCaptor.capture());
  }

  @Test
  public void enable_found_banned() {
    when(nodeDao.read(UUID)).thenReturn(generate(NodeInfo.Status.BANNED.name()));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> nodeManager.enable(UUID));
  }

  @Test
  public void enable_notFound() {
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> nodeManager.enable(UUID));
  }

  @Test
  public void disable_found_currentEnabled() {
    when(nodeDao.read(UUID)).thenReturn(generate(NodeInfo.Status.ENABLED.name()));
    when(clock.instant()).thenReturn(Instant.ofEpochMilli(System.currentTimeMillis()));
    nodeManager.disable(UUID);
    verify(nodeDao).update(nodeArgumentCaptor.capture());
    assertThat(nodeArgumentCaptor.getValue())
        .isNotNull()
        .hasFieldOrPropertyWithValue("status", NodeInfo.Status.DISABLED.name())
        .extracting("updateDate")
        .isNotNull();
  }

  @Test
  public void disable_found_currentDisabled() {
    when(nodeDao.read(UUID)).thenReturn(generate(NodeInfo.Status.DISABLED.name()));
    nodeManager.disable(UUID);
    verify(nodeDao, times(0)).update(nodeArgumentCaptor.capture());
  }

  @Test
  public void disable_found_banned() {
    when(nodeDao.read(UUID)).thenReturn(generate(NodeInfo.Status.BANNED.name()));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> nodeManager.disable(UUID));
  }

  @Test
  public void disable_notFound() {
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> nodeManager.disable(UUID));
  }

  private Node generate(final String status) {
    return ImmutableNode.builder().uuid(UUID)
        .createDate(Instant.ofEpochMilli(System.currentTimeMillis()))
        .status(status).uri(URI).build();
  }
}