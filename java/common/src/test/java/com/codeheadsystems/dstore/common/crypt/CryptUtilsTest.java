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

package com.codeheadsystems.dstore.common.crypt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CryptUtilsTest {

  private Random random;
  private CryptUtils cryptUtils;

  @BeforeEach
  void setUp() {
    random = new Random();
    cryptUtils = new CryptUtils(random);
  }

  @Test
  void base64() {
    final byte[] orig = cryptUtils.randomKey(1024);
    final String base64 = cryptUtils.toBase64(orig);
    final byte[] result = cryptUtils.fromBase64(base64);
    assertThat(orig).isEqualTo(result);
  }

  @Test
  public void xor() {
    final byte[] left = cryptUtils.randomKey();
    final byte[] right = cryptUtils.randomKey();
    final byte[] first = cryptUtils.xor(left, right);
    final byte[] second = cryptUtils.xor(first, right);
    assertThat(left).isNotEqualTo(first);
    assertThat(left).isEqualTo(second);
  }

  @Test
  public void xorStrings() {
    final byte[] first = cryptUtils.randomKey();
    final byte[] second = cryptUtils.randomKey();
    final byte[] third = cryptUtils.randomKey();
    final String firstStr = cryptUtils.toBase64(first);
    final String secondStr = cryptUtils.toBase64(second);
    final String thirdStr = cryptUtils.toBase64(third);
    final byte[] xorResult = cryptUtils.xor(firstStr, secondStr, thirdStr);
    byte[] expectation = cryptUtils.xor(first, second);
    expectation = cryptUtils.xor(expectation, third);
    assertThat(xorResult).isEqualTo(expectation);
  }

  @Test
  public void clean() {
    final byte[] array = new byte[12];
    Arrays.fill(array, (byte) 1);
    cryptUtils.clean(array);
    for (final byte b : array) {
      assertThat(b).isEqualTo((byte) 0);
    }
  }

}