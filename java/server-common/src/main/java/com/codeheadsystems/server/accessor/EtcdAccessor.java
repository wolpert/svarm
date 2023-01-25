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

package com.codeheadsystems.server.accessor;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ETCD accessor.
 */
@Singleton
public class EtcdAccessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(EtcdAccessor.class);

  private final Client client;

  /**
   * Constructor.
   *
   * @param client for etcd.
   */
  @Inject
  public EtcdAccessor(final Client client) {
    this.client = client;
  }

  /**
   * Puts the value to the etcd instance.
   *
   * @param namespace Type of value.
   * @param key       the key.
   * @param value     the value.
   */
  public void put(final String namespace, final String key, final String value) {
    LOGGER.trace("put({},{},{}", namespace, key, value);
    final String namespaceKey = String.format("%s/%s", namespace, key);
    client.getKVClient().put(
        ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)),
        ByteSequence.from(value.getBytes(StandardCharsets.UTF_8)));
  }



  /**
   * Delete the key from the etcd instance.
   *
   * @param namespace of the key.
   * @param key       the key.
   */
  public void delete(final String namespace, final String key) {
    LOGGER.trace("delete({},{}", namespace, key);
    final String namespaceKey = String.format("%s/%s", namespace, key);
    client.getKVClient().delete(ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Gets the key from the etcd instance. Waits forever.
   *
   * @param namespace of the key.
   * @param key       the key.
   * @return the value.
   */
  public Optional<String> get(final String namespace, final String key) {
    LOGGER.trace("get({},{}", namespace, key);
    final String namespaceKey = String.format("%s/%s", namespace, key);
    final CompletableFuture<GetResponse> future =
        client.getKVClient().get(ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)));
    try {
      final GetResponse getResponse = future.get();
      return getResponse.getKvs().stream()
          .map(KeyValue::getValue)
          .findFirst()
          .map(ByteSequence::toString);
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Unable to get from etcd {}", namespaceKey, e);
      throw new IllegalArgumentException(e);
    }
  }
}