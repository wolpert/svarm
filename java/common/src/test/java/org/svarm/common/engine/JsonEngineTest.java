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

package org.svarm.common.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonEngineTest {

  private static final Object OBJECT = new Object();
  private static final String STRING = "string";

  final TypeReference<Object> TYPE_REF = new TypeReference<>() {
  };
  @Mock private ObjectMapper objectMapper;
  @Mock private File file;
  @Mock private ObjectNode objectNode;

  @InjectMocks private JsonEngine jsonEngine;

  @Test
  void objectMapper() {
    assertThat(jsonEngine.objectMapper()).isEqualTo(objectMapper);
  }

  @Test
  void readValue_string_class() throws JsonProcessingException {
    when(objectMapper.readValue(STRING, Object.class)).thenReturn(OBJECT);

    assertThat(jsonEngine.readValue(STRING, Object.class)).isEqualTo(OBJECT);
  }

  @Test
  void readValue_string_class_exception() throws JsonProcessingException {
    when(objectMapper.readValue(STRING, Object.class)).thenThrow(new FakeJsonProcessingException());

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> jsonEngine.readValue(STRING, Object.class));
  }

  @Test
  void readValue_string_typeRef() throws JsonProcessingException {
    when(objectMapper.readValue(STRING, TYPE_REF)).thenReturn(OBJECT);

    assertThat(jsonEngine.readValue(STRING, TYPE_REF)).isEqualTo(OBJECT);
  }

  @Test
  void readValue_string_typeRef_exception() throws JsonProcessingException {
    when(objectMapper.readValue(STRING, TYPE_REF)).thenThrow(new FakeJsonProcessingException());

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> jsonEngine.readValue(STRING, TYPE_REF));
  }

  @Test
  void readValue_file_class() throws IOException {
    when(objectMapper.readValue(file, Object.class)).thenReturn(OBJECT);

    assertThat(jsonEngine.readValue(file, Object.class)).isEqualTo(OBJECT);
  }

  @Test
  void readValue_file_class_exception() throws IOException {
    when(objectMapper.readValue(file, Object.class)).thenThrow(new FakeJsonProcessingException());

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> jsonEngine.readValue(file, Object.class));
  }

  @Test
  void writeValue() throws JsonProcessingException {
    when(objectMapper.writeValueAsString(OBJECT)).thenReturn(STRING);

    assertThat(jsonEngine.writeValue(OBJECT)).isEqualTo(STRING);
  }

  @Test
  void writeValue_exception() throws JsonProcessingException {
    when(objectMapper.writeValueAsString(OBJECT)).thenThrow(new FakeJsonProcessingException());

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> jsonEngine.writeValue(OBJECT));
  }

  @Test
  void writeValue_file_instance() throws IOException {
    jsonEngine.writeValue(file, OBJECT);

    verify(objectMapper).writeValue(file, OBJECT);
  }

  @Test
  void writeValue_file_instance_exception() throws IOException {
    doThrow(new FakeJsonProcessingException()).when(objectMapper).writeValue(file, OBJECT);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> jsonEngine.writeValue(file, OBJECT));
  }

  @Test
  void createObjectNode() {
    when(objectMapper.createObjectNode()).thenReturn(objectNode);
    assertThat(jsonEngine.createObjectNode()).isEqualTo(objectNode);
  }

  class FakeJsonProcessingException extends JsonProcessingException {

    protected FakeJsonProcessingException() {
      super("boom");
    }
  }
}