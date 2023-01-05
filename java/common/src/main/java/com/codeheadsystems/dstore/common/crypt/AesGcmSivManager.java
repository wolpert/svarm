package com.codeheadsystems.dstore.common.crypt;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.GCMSIVBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses bouncy castle directly for AES/GCM/SIV encryption.
 */
@Singleton
public class AesGcmSivManager {

  /**
   * Length of keys to use by default.
   */
  public static final int KEY_LENGTH = 32;
  /**
   * Length of nonce to use by default.
   */
  public static final int NONCE_LENGTH = 12;
  private static final Logger LOGGER = LoggerFactory.getLogger(AesGcmSivManager.class);
  private final ThreadLocal<GCMSIVBlockCipher> cipherThreadLocal;
  private final CryptUtils cryptUtils;

  /**
   * Default constructor.
   *
   * @param cryptUtils for creating random keys.
   */
  @Inject
  public AesGcmSivManager(final CryptUtils cryptUtils) {
    LOGGER.info("AesGcmSivManager({})", cryptUtils);
    cipherThreadLocal = ThreadLocal.withInitial(GCMSIVBlockCipher::new);
    this.cryptUtils = cryptUtils;
  }

  private static void validate(final byte[] key, final byte[] nonce) {
    if (key.length != KEY_LENGTH) {
      throw new IllegalArgumentException("Key length not 32 bytes. It's " + key.length);
    }
    if (nonce.length != NONCE_LENGTH) {
      throw new IllegalArgumentException("Nonce length is not 12 bytes, it's " + nonce.length);
    }
  }

  /**
   * Encrypts a payload.
   *
   * @param payload to encrypt.
   * @param key     key to use.
   * @param nonce   nonce to use.
   * @return encrypted content.
   */
  public byte[] encrypt(final byte[] payload,
                        final byte[] key,
                        final byte[] nonce) {
    validate(key, nonce);
    final GCMSIVBlockCipher cipher = setupCrypto(key, nonce, true);
    return executeCrypto(payload, cipher);
  }

  /**
   * Decrypt a payload.
   *
   * @param payload encrypted payload.
   * @param key     key to use.
   * @param nonce   nonce to use.
   * @return decrypted payload.
   */
  public byte[] decrypt(final byte[] payload,
                        final byte[] key,
                        final byte[] nonce) {
    validate(key, nonce);
    final GCMSIVBlockCipher cipher = setupCrypto(key, nonce, false);
    return executeCrypto(payload, cipher);
  }

  /**
   * Provides for a secure random key.
   *
   * @return a random key.
   */
  public String randomKeyBase64Encoded() {
    return cryptUtils.toBase64(cryptUtils.randomKey(KEY_LENGTH));
  }

  /**
   * Provides for a secure random nonce.
   *
   * @return a random nonce.
   */
  public String randomNonceBase64Encoded() {
    return cryptUtils.toBase64(cryptUtils.randomKey(NONCE_LENGTH));
  }

  private byte[] executeCrypto(final byte[] payload, final GCMSIVBlockCipher cypher) {
    final int outputSize = cypher.getOutputSize(payload.length);
    final byte[] result = new byte[outputSize];
    int processed = cypher.processBytes(payload, 0, payload.length, result, 0);
    try {
      processed += cypher.doFinal(result, processed);
      LOGGER.trace("Avail:{} Processed:{} match:{}", outputSize, processed, outputSize == processed);
    } catch (InvalidCipherTextException e) {
      throw new IllegalArgumentException("Unable to execute cipher", e);
    } finally {
      cypher.reset();
    }
    return result;
  }

  private GCMSIVBlockCipher setupCrypto(final byte[] key, final byte[] nonce, final boolean encrypt) {
    final KeyParameter keyParameter = new KeyParameter(key);
    final CipherParameters parameters = new ParametersWithIV(keyParameter, nonce);
    final GCMSIVBlockCipher cipher = cipherThreadLocal.get();
    cipher.reset();
    cipher.init(encrypt, parameters);
    return cipher;
  }

}
