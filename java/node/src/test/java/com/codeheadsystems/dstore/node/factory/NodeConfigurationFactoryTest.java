package com.codeheadsystems.dstore.node.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.dstore.common.crypt.CryptUtils;
import com.codeheadsystems.dstore.common.engine.JsonEngine;
import com.codeheadsystems.dstore.common.factory.ObjectMapperFactory;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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