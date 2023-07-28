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

package org.svarm.common.crypt;

import java.util.Arrays;
import java.util.Base64;
import java.util.random.RandomGenerator;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities needed for crypt stuff with the product line.
 */
@Singleton
public class CryptUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CryptUtils.class);
  private static final int DEFAULT_KEY_BYTES = 256 / 8;

  private final RandomGenerator randomGenerator;

  /**
   * Default constructor.
   *
   * @param randomGenerator to use for the service. In production, must be SecureRandom.
   */
  @Inject
  public CryptUtils(final RandomGenerator randomGenerator) {
    LOGGER.info("Crypt({})", randomGenerator);
    this.randomGenerator = randomGenerator;
  }

  /**
   * Generates a random key assuming default bit length.
   *
   * @return the key.
   */
  public byte[] randomKey() {
    return randomKey(DEFAULT_KEY_BYTES);
  }

  /**
   * Generates a random key.
   *
   * @param numberOfBytes for the key.
   * @return the key.
   */
  public byte[] randomKey(final int numberOfBytes) {
    final byte[] result = new byte[numberOfBytes];
    randomGenerator.nextBytes(result);
    return result;
  }

  /**
   * Encodes the bytes to base64 encoding.
   *
   * @param bytes to encode.
   * @return base64. string
   */
  public String toBase64(final byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  /**
   * Decodes the bytes from base64 encoding.
   *
   * @param string the string to decode.
   * @return the bytes.
   */
  public byte[] fromBase64(final String string) {
    return Base64.getDecoder().decode(string);
  }

  /**
   * XORs keys that are base64 encoded.
   *
   * @param base64EncodeKeys to decode and XOR.
   * @return the XOR result.
   */
  public byte[] xor(final String... base64EncodeKeys) {
    if (base64EncodeKeys.length < 1) {
      throw new IllegalArgumentException("Must have at least one string");
    }
    final byte[] result = fromBase64(base64EncodeKeys[0]);
    for (int i = 1; i < base64EncodeKeys.length; i++) {
      final byte[] right = fromBase64(base64EncodeKeys[i]);
      for (int b = 0; b < right.length; b++) {
        result[b] ^= right[b];
      }
      clean(right);
    }
    return result;
  }

  /**
   * XOR two byte arrays.
   *
   * @param left  array.
   * @param right array.
   * @return a new array from XOR of the other two.
   */
  public byte[] xor(byte[] left, byte[] right) {
    if (left.length != right.length) {
      throw new IllegalArgumentException("Both byte arrays need to be the same");
    }
    final byte[] result = new byte[left.length];
    for (int i = 0; i < left.length; i++) {
      result[i] = (byte) (left[i] ^ right[i]);
    }
    return result;
  }

  /**
   * Nulls out a given byte array. (Reduce issue from dumping)
   *
   * @param array to null out.
   */
  public void clean(final byte[] array) {
    Arrays.fill(array, (byte) 0x0);
  }

}
