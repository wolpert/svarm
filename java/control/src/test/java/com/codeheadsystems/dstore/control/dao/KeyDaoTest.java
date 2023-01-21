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

package com.codeheadsystems.dstore.control.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.dstore.control.model.ImmutableKey;
import com.codeheadsystems.dstore.control.model.Key;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KeyDaoTest extends JdbiDaoTest<KeyDao> {

  private static Key generateKey() {
    final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());// Instant.now does weird things.
    return ImmutableKey.builder()
        .key("key").createDate(instant).id(UUID.randomUUID().toString()).nonce("nonce").build();
  }

  @Override
  protected Class<KeyDao> getDaoClass() {
    return KeyDao.class;
  }

  @Test
  public void roundTrip() {
    final Key key = generateKey();
    dao.insert(key);
    final Key result = dao.read(key.id());

    assertThat(result).isEqualTo(key);

    dao.delete(key.id());

    assertThat(dao.read(key.id())).isNull();
  }

}