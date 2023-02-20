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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.engine.NodeConfigurationEngine;
import org.svarm.control.dao.NodeRangeDao;
import org.svarm.control.engine.NodeAvailabilityEngine;
import org.svarm.control.model.NodeRange;
import org.svarm.server.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class NodeRangeManagerTest extends BaseMetricTest {

  private static final String TENANT = "TENANT";
  private static final String TABLE = "table";
  private static final String UUID = "uuid";
  private static final String STATUS = "status";
  private static final String VERSION = "version";
  @Mock private NodeRangeDao nodeRangeDao;
  @Mock private NodeManager nodeManager;
  @Mock private Clock clock;
  @Mock private List<String> list;
  @Mock private NodeAvailabilityEngine nodeAvailabilityEngine;
  @Mock private NodeConfigurationEngine nodeConfigurationEngine;
  @Mock private NodeRange nodeRange;
  @Captor private ArgumentCaptor<NodeRange> nodeRangeArgumentCaptor;

  private NodeRangeManager nodeRangeManager;

  @BeforeEach
  void setup() {
    nodeRangeManager = new NodeRangeManager(nodeRangeDao, clock, metrics, nodeAvailabilityEngine, nodeConfigurationEngine, nodeManager);
  }

  @Test
  void resources() {
    when(nodeRangeDao.resources(TENANT)).thenReturn(list);
    assertThat(nodeRangeManager.resources(TENANT)).isEqualTo(list);
  }

  @Test
  void setReady_notfound() {
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> nodeRangeManager.setReady(UUID, TENANT, TABLE, true));
  }

  @Test
  void setReady_found_ready_notAllRady() {
    mockIt(nodeRange, false);
    when(nodeRangeDao.read(UUID, TENANT, TABLE)).thenReturn(nodeRange);
    when(nodeRangeDao.nodeRanges(TENANT, TABLE)).thenReturn(List.of(nodeRange)); // with the false set.

    nodeRangeManager.setReady(UUID, TENANT, TABLE, true);

    verify(nodeRangeDao).update(nodeRangeArgumentCaptor.capture());
    assertThat(nodeRangeArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("ready", true);
  }

  @Test
  void setReady_found_notReady_notAllRady() {
    mockIt(nodeRange, false);
    when(nodeRangeDao.read(UUID, TENANT, TABLE)).thenReturn(nodeRange);
    when(nodeRangeDao.nodeRanges(TENANT, TABLE)).thenReturn(List.of(nodeRange)); // with the false set.

    nodeRangeManager.setReady(UUID, TENANT, TABLE, true);

    verify(nodeRangeDao).update(nodeRangeArgumentCaptor.capture());
    assertThat(nodeRangeArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("ready", true);
  }

  private void mockIt(final NodeRange nodeRange, final boolean ready) {
    when(nodeRange.nodeUuid()).thenReturn(UUID);
    when(nodeRange.createDate()).thenReturn(Instant.now());
    when(nodeRange.ready()).thenReturn(ready);
    when(nodeRange.resource()).thenReturn(TABLE);
    when(nodeRange.tenant()).thenReturn(TENANT);
    when(nodeRange.lowHash()).thenReturn(Integer.MIN_VALUE);
    when(nodeRange.highHash()).thenReturn(Integer.MAX_VALUE);
    when(nodeRange.status()).thenReturn(STATUS);
    when(nodeRange.tableVersion()).thenReturn(VERSION);
  }
}