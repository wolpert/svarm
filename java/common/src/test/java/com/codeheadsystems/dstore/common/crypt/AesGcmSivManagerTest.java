package com.codeheadsystems.dstore.common.crypt;

import static org.assertj.core.api.Assertions.assertThat;

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
  public void roundTrip(){
    final byte[] payload = bytes(1024);
    final byte[] key = bytes(AesGcmSivManager.KEY_LENGTH);
    final byte[] nonce = bytes(AesGcmSivManager.NONCE_LENGTH);
    random.nextBytes(payload);
    final byte[] encrypted = manager.encrypt(payload, key, nonce);
    final byte[] decrypted = manager.decrypt(encrypted, key, nonce);
    assertThat(payload).describedAs("encrypting works").isNotEqualTo(encrypted);
    assertThat(payload).describedAs("decrypting works").isEqualTo(decrypted);
  }

  private byte[] bytes(int length){
    final byte[] bytes = new byte[length];
    random.nextBytes(bytes);
    return bytes;
  }


}