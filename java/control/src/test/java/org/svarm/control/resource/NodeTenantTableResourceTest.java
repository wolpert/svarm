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

package org.svarm.control.resource;

import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.control.manager.NodeRangeManager;

@ExtendWith(MockitoExtension.class)
class NodeTenantTableResourceTest {

  private static final String TENANT = "TENANT";
  @Mock private NodeRangeManager nodeRangeManager;
  @Mock private List<String> list;

  @InjectMocks private NodeTenantTableResource nodeTenantTableResource;

  @Test
  public void listTenantTables() {
    when(nodeRangeManager.resources(TENANT)).thenReturn(list);
    Assertions.assertThat(nodeTenantTableResource.listResources(TENANT)).isEqualTo(list);
  }

}