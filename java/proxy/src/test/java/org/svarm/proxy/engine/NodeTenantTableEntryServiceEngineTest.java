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

package org.svarm.proxy.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.api.NodeRange;
import org.svarm.node.api.NodeTenantTableEntryService;
import org.svarm.node.javaclient.factory.NodeServiceFactory;
import org.svarm.proxy.ProxyConfiguration;

@ExtendWith(MockitoExtension.class)
class NodeTenantTableEntryServiceEngineTest {

  private static final String URL = "url";

  @Mock private NodeServiceFactory nodeServiceFactory;
  @Mock private NodeRange nodeRange;
  @Mock private NodeTenantTableEntryService service;
  @Mock private ProxyConfiguration proxyConfiguration;

  @InjectMocks private NodeTenantTableEntryServiceEngine engine;

  @Test
  void get() {
    when(nodeRange.uri()).thenReturn(URL);
    when(nodeServiceFactory.nodeService(URL)).thenReturn(service);

    assertThat(engine.get(nodeRange)).isEqualTo(service);
  }


}