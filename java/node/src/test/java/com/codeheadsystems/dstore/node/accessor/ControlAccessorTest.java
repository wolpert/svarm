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

package com.codeheadsystems.dstore.node.accessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import com.codeheadsystems.dstore.control.common.api.NodeInfo;
import com.codeheadsystems.metrics.test.BaseMetricTest;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControlAccessorTest extends BaseMetricTest {

  private static final String UUID = "UUID";
  private static final String STATUS = "status";
  @Mock private ControlNodeService controlNodeService;
  @Mock private NodeInfo nodeInfo;

  private ControlAccessor accessor;

  @BeforeEach
  void setup() {
    accessor = new ControlAccessor(controlNodeService, metrics);
  }

  @Test
  void status_notFound() {
    when(controlNodeService.status(UUID)).thenThrow(FeignException.NotFound.class);

    assertThat(accessor.status(UUID))
        .isEmpty();
  }

  @Test
  void status_found() {
    when(controlNodeService.status(UUID)).thenReturn(nodeInfo);
    when(nodeInfo.status()).thenReturn(STATUS);

    assertThat(accessor.status(UUID))
        .contains(STATUS);
  }

}