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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.jdbi.v3.sqlobject.transaction.TransactionalConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.engine.NodeConfigurationEngine;
import org.svarm.control.converter.NodeRangeConverter;
import org.svarm.control.dao.NodeRangeDao;
import org.svarm.control.engine.NodeAvailabilityEngine;
import org.svarm.control.engine.RingHashSplitEngine;
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
  @Mock private Clock clock;
  @Mock private List<String> list;
  @Mock private NodeAvailabilityEngine nodeAvailabilityEngine;
  @Mock private NodeConfigurationEngine nodeConfigurationEngine;
  @Mock private NodeRange nodeRange;
  @Mock private RingHashSplitEngine ringHashSplitEngine;
  @Mock private NodeRangeConverter nodeRangeConverter;
  @Captor private ArgumentCaptor<NodeRange> nodeRangeArgumentCaptor;

  private NodeRangeManager nodeRangeManager;

  @BeforeEach
  void setup() {
    nodeRangeManager = new NodeRangeManager(new FakeNodeRangeDao(nodeRangeDao), clock, metrics, nodeAvailabilityEngine,
        nodeConfigurationEngine, ringHashSplitEngine, nodeRangeConverter);
  }

  @Test
  void resources() {
    when(nodeRangeDao.resources(TENANT)).thenReturn(list);
    assertThat(nodeRangeManager.resources(TENANT)).isEqualTo(list);
  }

  @Test
  void finalizeDelete() {
    when(nodeRange.status()).thenReturn(NodeRange.STATUS_DELETING);
    when(nodeRangeDao.read(UUID, TENANT, TABLE)).thenReturn(nodeRange);
    nodeRangeManager.finalizeDelete(UUID, TENANT, TABLE);
    //verify(nodeRangeDao).delete(UUID, TENANT, TABLE); // TODO: Fix this test
  }

  @Test
  void finalizeDelete_wrongStatus() {
    when(nodeRange.status()).thenReturn(NodeRange.STATUS_INIT);
    when(nodeRangeDao.read(UUID, TENANT, TABLE)).thenReturn(nodeRange);
    nodeRangeManager.finalizeDelete(UUID, TENANT, TABLE);
    verify(nodeRangeDao, never()).delete(UUID, TENANT, TABLE);
  }

  @Test
  void finalizeDelete_notFound() {
    when(nodeRangeDao.read(UUID, TENANT, TABLE)).thenReturn(null);
    nodeRangeManager.finalizeDelete(UUID, TENANT, TABLE);
    verify(nodeRangeDao, never()).delete(UUID, TENANT, TABLE);
  }

  @Test
  void setReady_notfound() {
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> nodeRangeManager.setReady(UUID, TENANT, TABLE, true));
  }

  @Test
  void setReady_found_ready_notAllReady() {
    mockIt(nodeRange, false);
    when(nodeRangeDao.read(UUID, TENANT, TABLE)).thenReturn(nodeRange);
    when(nodeRangeDao.nodeRanges(TENANT, TABLE)).thenReturn(List.of(nodeRange)); // with the false set.

    nodeRangeManager.setReady(UUID, TENANT, TABLE, true);

    verify(nodeRangeDao).update(nodeRangeArgumentCaptor.capture());
    assertThat(nodeRangeArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("ready", true);
  }

  @Test
  void setReady_found_notReady_notAllReady() {
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
    when(nodeRange.hash()).thenReturn(Integer.MIN_VALUE);
    when(nodeRange.status()).thenReturn(STATUS);
    when(nodeRange.tableVersion()).thenReturn(VERSION);
  }

  static class FakeNodeRangeDao implements NodeRangeDao {

    private final NodeRangeDao dao;

    public FakeNodeRangeDao(final NodeRangeDao dao) {
      this.dao = dao;
    }

    @Override
    public <X extends Exception> void useTransaction(final TransactionalConsumer<NodeRangeDao, X> callback) throws X {
      callback.useTransaction(dao);
    }

    @Override
    public void insert(final NodeRange instance) {
      dao.insert(instance);
    }

    @Override
    public void update(final NodeRange instance) {
      dao.update(instance);
    }

    @Override
    public NodeRange read(final String nodeUuid, final String tenant, final String resource) {
      return dao.read(nodeUuid, tenant, resource);
    }

    @Override
    public List<NodeRange> nodeRanges(final String tenant, final String resource) {
      return dao.nodeRanges(tenant, resource);
    }

    @Override
    public List<NodeRange> nodeRanges(final String uuid) {
      return dao.nodeRanges(uuid);
    }

    @Override
    public List<org.svarm.common.config.api.NodeRange> apiNodeRanges(final String tenant, final String resource) {
      return dao.apiNodeRanges(tenant, resource);
    }

    @Override
    public List<String> tenants() {
      return dao.tenants();
    }

    @Override
    public List<String> resources(final String tenant) {
      return dao.resources(tenant);
    }

    @Override
    public int delete(final String nodeUuid, final String tenant, final String resource) {
      return dao.delete(nodeUuid, tenant, resource);
    }

    @Override
    public Handle getHandle() {
      return dao.getHandle();
    }

    @Override
    public <R, X extends Exception> R withHandle(final HandleCallback<R, X> callback) throws X {
      return dao.withHandle(callback);
    }
  }

}