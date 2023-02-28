package org.svarm.control.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RingHashSplitEngineTest {

  private RingHashSplitEngine engine = new RingHashSplitEngine();


  static Stream<Arguments> defaultTestCases() {
    return Stream.of(
        Arguments.of(1, List.of(Integer.MIN_VALUE)),
        Arguments.of(2, List.of(Integer.MIN_VALUE, 0)),
        Arguments.of(3, List.of(Integer.MIN_VALUE, -715827883, 715827882)),
        Arguments.of(4, List.of(Integer.MIN_VALUE, -1073741824, 0, 1073741824)),
        Arguments.of(5, List.of(Integer.MIN_VALUE, -1288490189, -429496730, 429496729, 1288490188))
    );
  }

  @ParameterizedTest
  @MethodSource("defaultTestCases")
  void testLowHash(final int repFactor, final List<Integer> lowHashHash) {
    assertThat(engine.evenSplitHashes(repFactor)).isEqualTo(lowHashHash).hasSize(repFactor);
  }

}