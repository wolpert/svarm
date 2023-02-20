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

package org.svarm.node.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.common.crypt.CryptUtils;
import org.svarm.common.engine.JsonEngine;
import org.svarm.common.factory.ObjectMapperFactory;
import org.svarm.node.model.NodeInternalConfiguration;

class NodeConfigurationFactoryTest {

  private NodeConfigurationFactory factory;

  @BeforeEach
  void setup() {
    final ObjectMapper objectMapper = new ObjectMapperFactory().generate();
    factory = new NodeConfigurationFactory(new CryptUtils(new Random()), new JsonEngine(objectMapper));
  }

  @Test
  void testTwice() throws IOException {
    doWithFile(Files.createTempDirectory(getClass().getSimpleName() + "-"), tmpDir -> {
      System.out.println("File: " + tmpDir);
      doWithFile(Path.of(tmpDir.toString(), NodeConfigurationFactory.CONFIGURATION_NAME), realFile -> {
        assertThat(realFile).doesNotExist();
        final NodeInternalConfiguration c1 = factory.readOrGenerate(tmpDir.toString());
        assertThat(realFile).exists();
        assertThat(c1).isNotNull();
        final NodeInternalConfiguration c2 = factory.readOrGenerate(tmpDir.toString());
        assertThat(realFile).exists();
        assertThat(c1).isEqualTo(c2);
      });
    });
  }

  private void doWithFile(final Path path, Consumer<Path> consumer) {
    try {
      consumer.accept(path);
    } finally {
      try {
        Files.delete(path);
      } catch (IOException e) {
        System.out.println("Warning on path: " + path + ":" + e.getMessage());
      }
    }
  }

}