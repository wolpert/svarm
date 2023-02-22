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

package org.svarm.node.api;

import com.codeheadsystems.test.model.BaseJacksonTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

class EntryInfoTest extends BaseJacksonTest<EntryInfo> {

  @Override
  protected Class<EntryInfo> getBaseClass() {
    return EntryInfo.class;
  }

  @Override
  protected EntryInfo getInstance() {
    try {
      return ImmutableEntryInfo.builder()
          .id("id")
          .locationHash(2)
          .timestamp(3L)
          .data(objectMapper.readValue("{\"a\":2}", JsonNode.class))
          .build();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}