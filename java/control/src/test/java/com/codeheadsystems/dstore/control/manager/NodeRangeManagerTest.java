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

package com.codeheadsystems.dstore.control.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.common.config.engine.NodeConfigurationEngine;
import com.codeheadsystems.dstore.control.dao.NodeRangeDao;
import com.codeheadsystems.dstore.control.engine.NodeAvailabilityEngine;
import com.codeheadsystems.metrics.test.BaseMetricTest;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NodeRangeManagerTest extends BaseMetricTest {

  private static final String TENANT = "TENANT";
  @Mock private NodeRangeDao nodeRangeDao;
  @Mock private NodeManager nodeManager;
  @Mock private Clock clock;
  @Mock private List<String> list;
  @Mock private NodeAvailabilityEngine nodeAvailabilityEngine;
  @Mock private NodeConfigurationEngine nodeConfigurationEngine;

  private NodeRangeManager nodeRangeManager;

  @BeforeEach
  void setup() {
    nodeRangeManager = new NodeRangeManager(nodeRangeDao, clock, metrics, nodeAvailabilityEngine, nodeConfigurationEngine);
  }

  @Test
  void resources() {
    when(nodeRangeDao.resources(TENANT)).thenReturn(list);
    assertThat(nodeRangeManager.resources(TENANT)).isEqualTo(list);
  }
}