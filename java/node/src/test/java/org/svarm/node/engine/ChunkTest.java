package org.svarm.node.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChunkTest {

  private Chunk<String> chunk;

  @BeforeEach
  void setUp() {
    chunk = new Chunk<>();
  }

  @Test
  void add() {
    chunk.add(1, "a");
    assertThat(chunk.getRows()).isEqualTo(1);
    chunk.add(1, "b");
    assertThat(chunk.getRows()).isEqualTo(2);
    chunk.add(2, "c");
    assertThat(chunk.getRows()).isEqualTo(3);
    assertThat(chunk.get(1)).isPresent().isPresent().contains(List.of("a", "b"));
    assertThat(chunk.get(2)).isPresent().contains(List.of("c"));
  }

  @Test
  void testAdd() {
    chunk.add(1, List.of("a", "b"));
    assertThat(chunk.getRows()).isEqualTo(2);
    chunk.add(2, List.of("c"));
    assertThat(chunk.getRows()).isEqualTo(3);
    assertThat(chunk.get(1)).isPresent().contains(List.of("a", "b"));
    assertThat(chunk.get(2)).isPresent().contains(List.of("c"));
  }

  @Test
  void hashes() {
    chunk.add(1, "a");
    chunk.add(1, "b");
    assertThat(chunk.hashes()).containsExactly(1);
    chunk.add(2, "c");
    assertThat(chunk.hashes()).containsExactly(1, 2);
  }

  @Test
  void isEmpty() {
    assertThat(chunk.isEmpty()).isTrue();
    chunk.add(1, "a");
    assertThat(chunk.isEmpty()).isFalse();
  }

  @Test
  void remove() {
    chunk.add(1, "a");
    chunk.add(1, "b");
    chunk.add(2, "c");
    assertThat(chunk.getRows()).isEqualTo(3);
    assertThat(chunk.get(1)).isPresent().contains(List.of("a", "b"));
    assertThat(chunk.get(2)).isPresent().contains(List.of("c"));
    final List<String> result = chunk.remove(1);
    assertThat(chunk.getRows()).isEqualTo(1);
    assertThat(chunk.get(1)).isEmpty();
    assertThat(chunk.get(2)).isPresent().contains(List.of("c"));
    assertThat(result).containsExactly("a", "b");
  }

  @Test
  void clear() {
    chunk.add(1, "a");
    chunk.add(1, "b");
    chunk.add(2, "c");
    assertThat(chunk.getRows()).isEqualTo(3);
    assertThat(chunk.get(1)).isPresent().contains(List.of("a", "b"));
    assertThat(chunk.get(2)).isPresent().contains(List.of("c"));
    chunk.clear();
    assertThat(chunk.getRows()).isEqualTo(0);
    assertThat(chunk.get(1)).isEmpty();
    assertThat(chunk.get(2)).isEmpty();
  }

  @Test
  void all() {
    chunk.add(1, "a");
    chunk.add(1, "b");
    chunk.add(2, "c");
    assertThat(chunk.all()).containsExactly("a", "b", "c");
  }
}