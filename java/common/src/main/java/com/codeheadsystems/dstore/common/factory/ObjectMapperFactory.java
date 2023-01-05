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

package com.codeheadsystems.dstore.common.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Builds a standard object mapper across terrapin.
 */
@Singleton
public class ObjectMapperFactory {

  /**
   * Default constructor.
   */
  @Inject
  public ObjectMapperFactory() {

  }

  /**
   * Generates the object mapper.
   *
   * @return an object mapper.
   */
  public ObjectMapper generate() {
    return new ObjectMapper()
        .registerModule(new Jdk8Module());
  }

}
