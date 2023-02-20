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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Base64;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AesGcmSivManagerTest {

  private Random random;
  private AesGcmSivManager manager;

  @BeforeEach
  void setUp() {
    random = new Random();
    manager = new AesGcmSivManager(new CryptUtils(new Random()));
  }

  @Test
  public void testRandomKeyLength() {
    final String value = manager.randomKeyBase64Encoded();
    final byte[] bytes = Base64.getDecoder().decode(value);
    assertThat(bytes)
        .hasSize(AesGcmSivManager.KEY_LENGTH);
  }

  @Test
  public void testRandomNonceLength() {
    final String value = manager.randomNonceBase64Encoded();
    final byte[] bytes = Base64.getDecoder().decode(value);
    assertThat(bytes)
        .hasSize(AesGcmSivManager.NONCE_LENGTH);
  }

  @Test
  public void roundTrip() {
    final byte[] payload = bytes(1024);
    final byte[] key = bytes(AesGcmSivManager.KEY_LENGTH);
    final byte[] nonce = bytes(AesGcmSivManager.NONCE_LENGTH);
    random.nextBytes(payload);
    final byte[] encrypted = manager.encrypt(payload, key, nonce);
    final byte[] decrypted = manager.decrypt(encrypted, key, nonce);
    assertThat(payload).describedAs("encrypting works").isNotEqualTo(encrypted);
    assertThat(payload).describedAs("decrypting works").isEqualTo(decrypted);
  }

  @Test
  public void roundTrip_badCipherText() {
    final byte[] payload = bytes(1024);
    final byte[] key = bytes(AesGcmSivManager.KEY_LENGTH);
    final byte[] nonce = bytes(AesGcmSivManager.NONCE_LENGTH);
    random.nextBytes(payload);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.decrypt(payload, key, nonce));
  }

  @Test
  public void roundTrip_badKeyLength() {
    final byte[] payload = bytes(1024);
    final byte[] key = bytes(AesGcmSivManager.KEY_LENGTH - 1);
    final byte[] nonce = bytes(AesGcmSivManager.NONCE_LENGTH);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.encrypt(payload, key, nonce));
  }

  @Test
  public void roundTrip_badNonceLength() {
    final byte[] payload = bytes(1024);
    final byte[] key = bytes(AesGcmSivManager.KEY_LENGTH);
    final byte[] nonce = bytes(AesGcmSivManager.NONCE_LENGTH - 1);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> manager.encrypt(payload, key, nonce));
  }

  private byte[] bytes(int length) {
    final byte[] bytes = new byte[length];
    random.nextBytes(bytes);
    return bytes;
  }


}