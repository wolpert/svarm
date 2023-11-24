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
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.svarm.control.common.api.NodeInfo;
import org.svarm.control.model.ImmutableNode;
import org.svarm.control.model.Node;

class NodeDaoTest extends JdbiDaoTest<NodeDao> {

  private static Node generateNode() {
    final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());// Instant.now does weird things.
    return ImmutableNode.builder().uuid(UUID.randomUUID().toString()).createDate(instant).uri("uri")
        .status("status").build();
  }

  @Override
  protected Class<NodeDao> getDaoClass() {
    return NodeDao.class;
  }

  @Test
  public void roundTrip() {
    final Node node = generateNode();
    dao.insert(node);
    final Node result = dao.read(node.uuid());

    assertThat(result).isEqualTo(node);

    dao.delete(node.uuid());

    Assertions.assertThat(dao.read(node.uuid())).isNull();
  }

  @Test
  public void update() {
    final Node node = generateNode();
    dao.insert(node);
    final Node result = dao.read(node.uuid());
    assertThat(result).isEqualTo(node);
    assertThat(dao.allNodes()).contains(node.uuid());
    assertThat(dao.allEnabledNodes()).doesNotContain(node.uuid());

    final Node node2 = ImmutableNode.copyOf(node).withStatus(NodeInfo.Status.ENABLED.name())
        .withUpdateDate(Instant.ofEpochMilli(System.currentTimeMillis() + 1000));
    dao.update(node2);
    final Node result2 = dao.read(node.uuid());
    assertThat(dao.allNodes()).contains(node.uuid());
    assertThat(dao.allEnabledNodes()).contains(node.uuid());
    assertThat(result2).isEqualTo(node2);
    assertThat(result2).isNotEqualTo(result);
  }

}