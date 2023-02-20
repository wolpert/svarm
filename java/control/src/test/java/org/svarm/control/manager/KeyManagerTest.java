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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.common.crypt.AesGcmSivManager;
import org.svarm.common.crypt.CryptUtils;
import org.svarm.control.dao.JdbiDaoTest;
import org.svarm.control.dao.KeyDao;
import org.svarm.control.model.ImmutableKey;
import org.svarm.control.model.Key;

class KeyManagerTest extends JdbiDaoTest<KeyDao> {
  private static final String NODE = "node";
  private static final String TENANT = "tenant";


  private KeyManager manager;

  @Override
  protected Class<KeyDao> getDaoClass() {
    return KeyDao.class;
  }

  @BeforeEach
  void setupManager() {
    final AesGcmSivManager aesGcmSivManager = new AesGcmSivManager(new CryptUtils(new Random()));
    manager = new KeyManager(dao, aesGcmSivManager, Clock.systemUTC());
  }

  @Test
  void testNoSuchKey_node() {
    final Key result = manager.getNodeKey(NODE);
    assertThat(result).isNotNull();
  }

  @Test
  void testNoSuchKey_nodeTenant() {
    final Key result = manager.getNodeKey(NODE, TENANT);
    assertThat(result).isNotNull();
  }

  @Test
  void testKeyFound_node() {
    final Key key = ImmutableKey.builder()
        .id("node:" + NODE)
        .key("key")
        .nonce("nonce")
        .createDate(Instant.ofEpochMilli(System.currentTimeMillis()))
        .build();
    dao.insert(key);
    assertThat(manager.getNodeKey(NODE))
        .isEqualTo(key);
  }

}