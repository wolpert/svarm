/*
 * Copyright (c) 2022-2023. Ned Wolpert
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

package com.codeheadsystems.dstore.common.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps checked exceptions with the object mapper.
 */
@Singleton
public class JsonEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonEngine.class);

  private final ObjectMapper objectMapper;

  /**
   * Default constructor.
   *
   * @param objectMapper we are wrapping.
   */
  @Inject
  public JsonEngine(final ObjectMapper objectMapper) {
    LOGGER.info("JsonManager({})", objectMapper);
    this.objectMapper = objectMapper;
  }

  /**
   * Returns the object mapper that we are using.
   *
   * @return object mapper.
   */
  public ObjectMapper objectMapper() {
    return objectMapper;
  }

  /**
   * Wrapper so no one has to catch the JSON processing exception. Safe logging, only logs the class, not the json.
   *
   * @param json  to convert.
   * @param clazz to convert.
   * @param <T>   the type.
   * @return an instance of the type.
   */
  public <T> T readValue(final String json, final Class<T> clazz) {
    LOGGER.debug("readValue(json,{})", clazz);
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      LOGGER.error("Unable to read value for class: {}", clazz, e);
      throw new IllegalArgumentException("Unable to read value", e);
    }
  }

  /**
   * Wrapper so no one has to catch the JSON processing exception. Safe logging, only logs the class, not the json.
   *
   * @param json          to convert.
   * @param typeReference to convert.
   * @param <T>           the type.
   * @return an instance of the type.
   */
  public <T> T readValue(final String json, final TypeReference<T> typeReference) {
    LOGGER.debug("readValue(json,{})", typeReference);
    try {
      return objectMapper.readValue(json, typeReference);
    } catch (JsonProcessingException e) {
      LOGGER.error("Unable to read value for class: {}", typeReference, e);
      throw new IllegalArgumentException("Unable to read value", e);
    }
  }

  /**
   * Writes the value to the file in question.
   *
   * @param configurationFile to write to.
   * @param object            we want to write into JSON.
   * @param <T>               type of object we are wriing.
   */
  public <T> void writeValue(final File configurationFile,
                             final T object) {
    LOGGER.debug("writeValue(file,{})", configurationFile);
    try {
      objectMapper.writeValue(configurationFile, object);
    } catch (IOException e) {
      LOGGER.error("Unable to write value to file: {}", configurationFile, e);
      throw new IllegalArgumentException("Unable to read value", e);
    }
  }

  /**
   * Reads the file and returns an object based on the class.
   *
   * @param configureFile to read.
   * @param clazz         we are converting to.
   * @param <T>           the type of object.
   * @return an instance of the clazz.
   */
  public <T> T readValue(final File configureFile,
                         final Class<T> clazz) {
    LOGGER.debug("readValue(file,{})", clazz);
    try {
      return objectMapper.readValue(configureFile, clazz);
    } catch (IOException e) {
      LOGGER.error("Unable to read value for class: {}", clazz, e);
      throw new IllegalArgumentException("Unable to read value", e);
    }
  }

  /**
   * Writes the value to json.
   *
   * @param object the value to read
   * @param <T>    the type of object it is.
   * @return json.
   */
  public <T> String writeValue(final T object) {
    LOGGER.debug("writeValue(json,{})", object.getClass());
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      LOGGER.error("Unable to write value for class: {}", object.getClass(), e);
      throw new IllegalArgumentException("Unable to write value", e);
    }
  }

  /**
   * Generates a object node.
   *
   * @return an object node.
   */
  public ObjectNode createObjectNode() {
    return objectMapper.createObjectNode();
  }
}
