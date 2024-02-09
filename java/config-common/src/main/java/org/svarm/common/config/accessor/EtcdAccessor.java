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

package org.svarm.common.config.accessor;

import static org.svarm.common.config.module.EtcdModule.INTERNAL_ETCD_ACCESSOR_PREAMBLE;

import com.codeheadsystems.metrics.Metrics;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.micrometer.core.instrument.Tags;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
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
  private final String namespaceKeyFormat;
  private final Metrics metrics;

  /**
   * Constructor.
   *
   * @param client   for etcd.
   * @param preamble for us to use.
   * @param metrics  the metrics
   */
  @Inject
  public EtcdAccessor(final Client client,
                      @Named(INTERNAL_ETCD_ACCESSOR_PREAMBLE) final String preamble,
                      final Metrics metrics) {
    this.client = client;
    this.namespaceKeyFormat = preamble + "_%s/%s";
    this.metrics = metrics;
    LOGGER.info("EtcdAccessor({},{},{})", namespaceKeyFormat, client, metrics);
  }

  private String getNamespaceKey(final String namespace, final String key) {
    return String.format(namespaceKeyFormat, namespace, key);
  }

  /**
   * Puts the value to the etcd instance.
   *
   * @param namespace Type of value.
   * @param key       the key.
   * @param value     the value.
   */
  public void put(final String namespace, final String key, final String value) {
    final String namespaceKey = getNamespaceKey(namespace, key);
    LOGGER.trace("put({},{})", namespaceKey, value);
    metrics.time("etcd.put", Tags.of("namespace", namespace, "key", key), () -> {
      try {
        client.getKVClient().put(
                ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)),
                ByteSequence.from(value.getBytes(StandardCharsets.UTF_8)))
            .get();
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.error("Unable to get from etcd {}", namespaceKey, e);
        throw new IllegalArgumentException(e);
      }
      return null;
    });
  }

  /**
   * Puts the value to the etcd instance.
   *
   * @param namespace Type of value.
   * @param map       the key/value.
   */
  public void putAll(final String namespace, final Map<String, String> map) {
    LOGGER.trace("putAll({},{}", namespace, map);
    final KV kv = client.getKVClient();
    final Txn txn = kv.txn();
    metrics.time("etcd.putAll", Tags.of("namespace", namespace), () -> {
      try {
        map.entrySet().stream()
            .map(e -> Map.entry(getNamespaceKey(namespace, e.getKey()), e.getValue()))
            .map(e -> Map.entry(ByteSequence.from(e.getKey().getBytes()), ByteSequence.from(e.getValue().getBytes())))
            .forEach(e ->
                txn.Then(Op.put(e.getKey(), e.getValue(), PutOption.DEFAULT)));
        txn.commit().get();
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.error("Unable to get from etcd {}", namespace, e);
        throw new IllegalArgumentException(e);
      }
      return null;
    });
  }


  /**
   * Delete the key from the etcd instance.
   *
   * @param namespace of the key.
   * @param key       the key.
   */
  public void delete(final String namespace, final String key) {
    final String namespaceKey = getNamespaceKey(namespace, key);
    LOGGER.trace("delete({})", namespaceKey);
    metrics.time("etcd.delete", Tags.of("namespace", namespace, "key", key), () -> {
      try {
        client.getKVClient()
            .delete(ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)))
            .get();
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.error("Unable to delete from etcd {}", namespaceKey, e);
        throw new IllegalArgumentException(e);
      }
      return null;
    });
  }

  /**
   * Returns a watcher for the given arguments.
   *
   * @param namespace you are watching.
   * @param key       you want to watch. (Can be empty string)
   * @param listener  who gets the events.
   * @return watcher so you can cancel it.
   */
  public Watch.Watcher watch(final String namespace,
                             final String key,
                             final Watch.Listener listener) {
    final String namespaceKey = getNamespaceKey(namespace, key);
    LOGGER.trace("watch({})", namespaceKey);
    return metrics.time("etcd.watch", Tags.of("namespace", namespace, "key", key), () -> {
      final ByteSequence namespaceKeyBytes = ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8));
      final WatchOption watchOption = WatchOption.builder().isPrefix(true).build();
      final Watch watch = client.getWatchClient();
      return watch.watch(namespaceKeyBytes, watchOption, listener);
    });
  }

  /**
   * Gets the key from the etcd instance. Waits forever.
   * TODO: This should be a timeout option.
   *
   * @param namespace of the key.
   * @param key       the key.
   * @return the value.
   */
  public Optional<String> get(final String namespace, final String key) {
    final String namespaceKey = getNamespaceKey(namespace, key);
    LOGGER.trace("get({})", namespaceKey);
    return metrics.time("etcd.get", Tags.of("namespace", namespace, "key", key), () -> {
      final CompletableFuture<GetResponse> future =
          client.getKVClient().get(ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)));
      try {
        final GetResponse getResponse = future.get(100, TimeUnit.MILLISECONDS);
        return getResponse.getKvs().stream()
            .map(KeyValue::getValue)
            .findFirst()
            .map(ByteSequence::toString);
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.error("Unable to get from etcd {}", namespaceKey, e);
        throw new IllegalArgumentException(e);
      } catch (TimeoutException e) {
        LOGGER.info("Not found in etcd {}", namespaceKey);
        return Optional.empty();
      }
    });
  }

  /**
   * Gets the values from the etcd instance. Waits forever. Since you can use prefixes, this will
   * get all the keys as a prefix.
   *
   * @param namespace of the key.
   * @param key       the key.
   * @return the values.
   */
  public Map<String, String> getAll(final String namespace, final String key) {
    final String namespaceKey = getNamespaceKey(namespace, key);
    LOGGER.trace("getAll({})", namespaceKey);
    return metrics.time("etcd.getAll", Tags.of("namespace", namespace, "key", key), () -> {
      final ByteSequence byteSequenceKey = ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8));
      final GetOption getOption = GetOption.builder().isPrefix(true).build();
      final CompletableFuture<GetResponse> future =
          client.getKVClient().get(byteSequenceKey, getOption);
      try {
        return future.get(1, TimeUnit.SECONDS).getKvs().stream()
            .collect(Collectors.toMap(
                kv -> kv.getKey().toString(),
                kv -> kv.getValue().toString()
            ));
      } catch (TimeoutException | InterruptedException | ExecutionException e) {
        LOGGER.error("Unable to get from etcd {}", namespaceKey, e);
        throw new IllegalArgumentException(e);
      }
    });
  }
}