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

package com.codeheadsystems.dstore.endtoend.simple;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.dstore.endtoend.EnvironmentManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class InitializationTest {

  @Test
  void testInitialization() throws JsonProcessingException {
    final Map<String, Throwable> failure = EnvironmentManager.getInitializationFailure();
    final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    assertThat(failure)
        .describedAs("Initialization failure: {}", mapper.writeValueAsString(failure))
        .isEmpty();
  }

}
