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

package org.svarm.control.manager;

import java.time.Clock;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.crypt.AesGcmSivManager;
import org.svarm.control.dao.KeyDao;
import org.svarm.control.model.ImmutableKey;
import org.svarm.control.model.Key;

/**
 * Manages the keys in the application.
 */
@Singleton
public class KeyManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyManager.class);

  private final KeyDao keyDao;
  private final AesGcmSivManager aesGcmSivManager;
  private final Clock clock;

  /**
   * Constructor.
   *
   * @param keyDao           the dao.
   * @param aesGcmSivManager for encryption.
   * @param clock            our clock.
   */
  @Inject
  public KeyManager(final KeyDao keyDao, final AesGcmSivManager aesGcmSivManager, final Clock clock) {
    LOGGER.info("KeyManager({},{})", keyDao, aesGcmSivManager);
    this.keyDao = keyDao;
    this.aesGcmSivManager = aesGcmSivManager;
    this.clock = clock;
  }

  /**
   * Gets the key from the dao for the node.
   *
   * @param nodeUuid for the identifier.
   * @return the key.
   */
  public Key getNodeKey(final String nodeUuid) {
    LOGGER.trace("getNodeKey({})", nodeUuid);
    final String id = String.format("node:%s", nodeUuid);
    return getKeyByIdentifier(id);
  }

  /**
   * Gets the key from the dao for the node.
   *
   * @param nodeUuid  for the identifier.
   * @param reference for the identifier.
   * @return the key.
   */
  public Key getNodeKey(final String nodeUuid, final String reference) {
    LOGGER.trace("getNodeKey({},{})", nodeUuid, reference);
    final String id = String.format("node:%s:%s", nodeUuid, reference);
    return getKeyByIdentifier(id);
  }

  /**
   * Returns the key from the database.
   * TODO: ensure this idempotent regardless of threads or instances.
   *
   * @param id to lookup.
   * @return the key.
   */
  private Key getKeyByIdentifier(final String id) {
    final Key foundKey = keyDao.read(id);
    if (foundKey == null) {
      LOGGER.trace("building new key: {}", id);
      final Key generatedKey = ImmutableKey.builder().id(id)
          .key(aesGcmSivManager.randomKeyBase64Encoded())
          .nonce(aesGcmSivManager.randomNonceBase64Encoded())
          .createDate(clock.instant())
          .build();
      keyDao.insert(generatedKey);
      return generatedKey;
    } else {
      LOGGER.trace("returning old key: {}", id);
      return foundKey;
    }
  }

}
