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
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.model.RingEntry;

@ExtendWith(MockitoExtension.class)
class RingEngineTest {

  private static final String ID = "id";

  @Mock private HashingEngine hashingEngine;

  static Stream<Arguments> testValues() {
    return Stream.of(
        Arguments.of(1, 10, Set.of(10)),
        Arguments.of(2, 10, Set.of(10, -2147483639)),
        Arguments.of(3, 10, Set.of(10, -1431655757, 1431655774)),
        Arguments.of(4, 10, Set.of(10, 1073741833, -2147483639, -1073741816)),
        Arguments.of(1, 0, Set.of(0)),
        Arguments.of(2, 0, Set.of(0, 2147483646)),
        Arguments.of(3, 0, Set.of(0, 1431655764, -1431655767)),
        Arguments.of(4, 0, Set.of(0, 1073741823, 2147483646, -1073741826)),
        Arguments.of(1, 1, Set.of(1)),
        Arguments.of(2, 1, Set.of(1, 2147483647)),
        Arguments.of(3, 1, Set.of(1, 1431655765, -1431655766)),
        Arguments.of(4, 1, Set.of(1, 1073741824, 2147483647, -1073741825)),
        Arguments.of(1, 2, Set.of(2)),
        Arguments.of(2, 2, Set.of(2, -2147483647)),
        Arguments.of(3, 2, Set.of(2, 1431655766, -1431655765)),
        Arguments.of(4, 2, Set.of(2, 1073741825, -2147483647, -1073741824)),
        Arguments.of(1, -1, Set.of(-1)),
        Arguments.of(2, -1, Set.of(-1, 2147483645)),
        Arguments.of(3, -1, Set.of(-1, 1431655763, -1431655768)),
        Arguments.of(4, -1, Set.of(-1, 1073741822, 2147483645, -1073741827)),
        Arguments.of(1, -2, Set.of(-2)),
        Arguments.of(2, -2, Set.of(-2, 2147483644)),
        Arguments.of(3, -2, Set.of(-2, 1431655762, -1431655769)),
        Arguments.of(4, -2, Set.of(-2, 1073741821, 2147483644, -1073741828)),
        Arguments.of(20, 200, Set.of(200, 214748564, 429496928, 644245292, 858993656, 1073742020, 1288490384,
            1503238748, 1717987112, 1932735476, -2147483455, -1932735091, -1717986727, -1503238363, -1288489999,
            -1073741635, -858993271, -644244907, -429496543, -214748179))
    );
  }

  @ParameterizedTest
  @MethodSource("testValues")
  void runTest(final Integer repFactor, final Integer hashedValue, final Set<Integer> expected) {
    final RingEngine ringEngine = new RingEngine(hashingEngine);
    when(hashingEngine.murmur3(ID)).thenReturn(hashedValue);
    final RingEntry result = ringEngine.ringEntry(ID, repFactor);
    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", ID)
        .hasFieldOrPropertyWithValue("hash", hashedValue)
        .hasFieldOrPropertyWithValue("locationStores", expected);
  }

}