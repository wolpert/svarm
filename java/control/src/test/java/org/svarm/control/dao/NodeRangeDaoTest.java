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

package org.svarm.control.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.svarm.control.model.ImmutableNodeRange;
import org.svarm.control.model.NodeRange;

class NodeRangeDaoTest extends JdbiDaoTest<NodeRangeDao> {
  private static final String UUID1 = "uuid1";
  private static final String UUID2 = "uuid2";
  private static final String TENANT = "tenant";
  private static final String RESOURCE1 = "resource1";
  private static final String RESOURCE2 = "resource2";
  private static final String STATUS = "status";
  private static final String TABLE_VERSION = "tableVersion";

  private static Instant instant() {
    return Instant.ofEpochMilli(System.currentTimeMillis());
  }

  @Override
  protected Class<NodeRangeDao> getDaoClass() {
    return NodeRangeDao.class;
  }

  @Test
  void roundTrip() {
    final NodeRange nr1 = ImmutableNodeRange.builder().nodeUuid(UUID1).tenant(TENANT).resource(RESOURCE1)
        .createDate(instant()).ready(false).status(STATUS).hash(10).tableVersion(TABLE_VERSION).build();
    final NodeRange nr2 = ImmutableNodeRange.builder().nodeUuid(UUID2).tenant(TENANT)
        .resource(RESOURCE1).createDate(instant()).ready(true).status(STATUS).hash(20).tableVersion(TABLE_VERSION).build();
    final NodeRange nr3 = ImmutableNodeRange.builder().nodeUuid(UUID2).tenant(TENANT)
        .resource(RESOURCE2).createDate(instant()).ready(true).status(STATUS).hash(20).tableVersion(TABLE_VERSION).build();
    dao.insert(nr1);
    dao.insert(nr2);
    dao.insert(nr3);
    Assertions.assertThat(dao.read(UUID1, TENANT, RESOURCE1)).isEqualTo(nr1);
    Assertions.assertThat(dao.read(UUID2, TENANT, RESOURCE1)).isEqualTo(nr2);
    Assertions.assertThat(dao.read(UUID2, TENANT, RESOURCE2)).isEqualTo(nr3);

    Assertions.assertThat(dao.nodeRanges(UUID2))
        .hasSize(2)
        .contains(nr2, nr3);

    Assertions.assertThat(dao.nodeRanges(TENANT, RESOURCE1))
        .hasSize(2)
        .contains(nr1, nr2);

    assertThat(dao.tenants())
        .hasSize(1)
        .contains(TENANT);

    assertThat(dao.resources(TENANT))
        .hasSize(2)
        .contains(RESOURCE1, RESOURCE2);

    dao.delete(UUID2, TENANT, RESOURCE2);
    Assertions.assertThat(dao.read(UUID2, TENANT, RESOURCE2)).isNull();
    Assertions.assertThat(dao.nodeRanges(UUID2))
        .hasSize(1)
        .contains(nr2);
  }
}