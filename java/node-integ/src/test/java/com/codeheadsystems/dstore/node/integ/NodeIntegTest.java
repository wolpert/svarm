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

package com.codeheadsystems.dstore.node.integ;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.dstore.node.Node;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.integ.util.DeletingFileVisitor;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NodeIntegTest {

  private static DropwizardTestSupport<NodeConfiguration> SUPPORT;
  private static Path BASE_DIRECTORY_PATH;

  @BeforeAll
  static void setup() throws Exception {
    BASE_DIRECTORY_PATH = Files.createTempDirectory("NodeIntegTest-");
    System.out.println("Made directory: " + BASE_DIRECTORY_PATH.toString());
    SUPPORT = new DropwizardTestSupport<>(
        Node.class,
        ResourceHelpers.resourceFilePath("config.yaml"),
        ConfigOverride.config("databaseDirectory", BASE_DIRECTORY_PATH.toString())
    );
    SUPPORT.before();
  }

  @AfterAll
  static void tearDown() {
    SUPPORT.after();
    try {
      Files.walkFileTree(BASE_DIRECTORY_PATH, new DeletingFileVisitor());
    } catch (IOException re) {
      System.out.println("Log: Unable to delete " + BASE_DIRECTORY_PATH + ":" + re.getClass());
    }
    BASE_DIRECTORY_PATH = null;
  }

  @Test
  void doSomething() {
    assertThat(true).isTrue();
  }
}
