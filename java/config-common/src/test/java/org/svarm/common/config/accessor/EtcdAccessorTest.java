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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EtcdAccessorTest extends BaseMetricTest {

  private static final String PREAMBLE = "p";
  private static final String VALUE = "value";
  private static final String KEY = "key";
  private static final String NAMESPACE = "Namespace";

  @Mock private Client client;
  @Mock private KV kv;
  @Mock private Watch watch;
  @Mock private Watch.Listener listener;
  @Mock private Watch.Watcher watcher;
  @Mock private Txn txn;
  @Mock private KeyValue keyValue;
  @Mock private GetResponse getResponse;
  @Mock private CompletableFuture<PutResponse> putResponseCompletableFuture;
  @Mock private CompletableFuture<DeleteResponse> deleteResponseCompletableFuture;
  @Mock private CompletableFuture<GetResponse> getResponseCompletableFuture;
  @Mock private CompletableFuture<TxnResponse> txnResponseCompletableFuture;

  @Captor private ArgumentCaptor<ByteSequence> byteSequenceArgumentCaptor;
  @Captor private ArgumentCaptor<Op> opArgumentCaptor;
  @Captor private ArgumentCaptor<WatchOption> watchOptionArgumentCaptor;
  @Captor private ArgumentCaptor<GetOption> getOptionArgumentCaptor;
  @Captor private ArgumentCaptor<PutOption> putOptionArgumentCaptor;

  private EtcdAccessor accessor;

  @NotNull
  private static ByteSequence getNamespaceKeyBytes() {
    final String namespaceKey = PREAMBLE + "_" + NAMESPACE + "/" + KEY;
    return ByteSequence.from(namespaceKey.getBytes());
  }

  @BeforeEach
  void setUp() {
    accessor = new EtcdAccessorImpl(client, PREAMBLE, metrics);
  }

  @Test
  void put() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.put(byteSequenceArgumentCaptor.capture(), byteSequenceArgumentCaptor.capture())).thenReturn(putResponseCompletableFuture);
    accessor.put(NAMESPACE, KEY, VALUE);
    verify(putResponseCompletableFuture).get();
    List<String> values = byteSequenceArgumentCaptor.getAllValues().stream().map(Objects::toString).toList();
    assertThat(values).containsExactly(PREAMBLE + "_" + NAMESPACE + "/" + KEY, VALUE);
  }

  @Test
  void put_interrupted() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.put(byteSequenceArgumentCaptor.capture(), byteSequenceArgumentCaptor.capture())).thenReturn(putResponseCompletableFuture);
    when(putResponseCompletableFuture.get()).thenThrow(new InterruptedException());
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.put(NAMESPACE, KEY, VALUE));
  }

  @Test
  void put_execution() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.put(byteSequenceArgumentCaptor.capture(), byteSequenceArgumentCaptor.capture())).thenReturn(putResponseCompletableFuture);
    when(putResponseCompletableFuture.get()).thenThrow(new ExecutionException(new NullPointerException()));
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.put(NAMESPACE, KEY, VALUE));
  }

  @Test
  void putAll() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.txn()).thenReturn(txn);
    when(txn.Then(opArgumentCaptor.capture())).thenReturn(txn);
    when(txn.commit()).thenReturn(txnResponseCompletableFuture);
    accessor.putAll(NAMESPACE, Map.of(KEY, VALUE));
    verify(txnResponseCompletableFuture).get();
    assertThat(opArgumentCaptor.getValue())
        .isInstanceOf(Op.PutOp.class);
  }

  @Test
  void putAll_interrupted() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.txn()).thenReturn(txn);
    when(txn.Then(opArgumentCaptor.capture())).thenReturn(txn);
    when(txn.commit()).thenReturn(txnResponseCompletableFuture);
    when(txnResponseCompletableFuture.get()).thenThrow(new InterruptedException());
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.putAll(NAMESPACE, Map.of(KEY, VALUE)));
  }

  @Test
  void putAll_execution() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.txn()).thenReturn(txn);
    when(txn.Then(opArgumentCaptor.capture())).thenReturn(txn);
    when(txn.commit()).thenReturn(txnResponseCompletableFuture);
    when(txnResponseCompletableFuture.get()).thenThrow(new ExecutionException(new NullPointerException()));
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.putAll(NAMESPACE, Map.of(KEY, VALUE)));
  }

  @Test
  void delete() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.delete(byteSequenceArgumentCaptor.capture())).thenReturn(deleteResponseCompletableFuture);
    accessor.delete(NAMESPACE, KEY);
    verify(deleteResponseCompletableFuture).get();
    List<String> values = byteSequenceArgumentCaptor.getAllValues().stream().map(Objects::toString).toList();
    assertThat(values).containsExactly(PREAMBLE + "_" + NAMESPACE + "/" + KEY);
  }

  @Test
  void delete_interrupted() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.delete(byteSequenceArgumentCaptor.capture())).thenReturn(deleteResponseCompletableFuture);
    when(deleteResponseCompletableFuture.get()).thenThrow(new InterruptedException());
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.delete(NAMESPACE, KEY));
  }

  @Test
  void delete_execution() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.delete(byteSequenceArgumentCaptor.capture())).thenReturn(deleteResponseCompletableFuture);
    when(deleteResponseCompletableFuture.get()).thenThrow(new ExecutionException(new NullPointerException()));
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.delete(NAMESPACE, KEY));
  }

  @Test
  void watch() {
    when(client.getWatchClient()).thenReturn(watch);
    when(watch.watch(eq(getNamespaceKeyBytes()), watchOptionArgumentCaptor.capture(), eq(listener))).thenReturn(watcher);

    assertThat(accessor.watch(NAMESPACE, KEY, listener)).isEqualTo(watcher);
    assertThat(watchOptionArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("prefix", true);
  }

  @Test
  void get() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(getNamespaceKeyBytes())).thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(100, TimeUnit.MILLISECONDS)).thenReturn(getResponse);
    when(getResponse.getKvs()).thenReturn(List.of(keyValue));
    when(keyValue.getValue()).thenReturn(ByteSequence.from(VALUE.getBytes()));
    assertThat(accessor.get(NAMESPACE, KEY))
        .isPresent()
        .contains(VALUE);
  }

  @Test
  void get_interrupted() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(getNamespaceKeyBytes())).thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(100, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.get(NAMESPACE, KEY));
  }

  @Test
  void get_execution() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(getNamespaceKeyBytes())).thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(100, TimeUnit.MILLISECONDS)).thenThrow(new ExecutionException(new NullPointerException()));
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.get(NAMESPACE, KEY));
  }

  @Test
  void getAll() throws ExecutionException, InterruptedException, TimeoutException {
    final ByteSequence namespaceKeyBytes = getNamespaceKeyBytes();
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(eq(namespaceKeyBytes), getOptionArgumentCaptor.capture()))
        .thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(anyLong(), any())).thenReturn(getResponse);
    when(getResponse.getKvs()).thenReturn(List.of(keyValue));
    when(keyValue.getKey()).thenReturn(namespaceKeyBytes);
    when(keyValue.getValue()).thenReturn(ByteSequence.from(VALUE.getBytes()));
    assertThat(accessor.getAll(NAMESPACE, KEY))
        .contains(Map.entry(namespaceKeyBytes.toString(), VALUE));
    assertThat(getOptionArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("prefix", true);
  }

  @Test
  void getAll_interrupted() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(eq(getNamespaceKeyBytes()), getOptionArgumentCaptor.capture()))
        .thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(anyLong(), any())).thenThrow(new InterruptedException());
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.getAll(NAMESPACE, KEY));
    assertThat(getOptionArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("prefix", true);
  }

  @Test
  void getAll_execution() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(eq(getNamespaceKeyBytes()), getOptionArgumentCaptor.capture()))
        .thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(anyLong(), any())).thenThrow(new ExecutionException(new NullPointerException()));
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.getAll(NAMESPACE, KEY));
    assertThat(getOptionArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("prefix", true);
  }

  @Test
  void getAll_timeout() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(eq(getNamespaceKeyBytes()), getOptionArgumentCaptor.capture()))
        .thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(anyLong(), any())).thenThrow(new ExecutionException(new TimeoutException()));
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> accessor.getAll(NAMESPACE, KEY));
    assertThat(getOptionArgumentCaptor.getValue())
        .hasFieldOrPropertyWithValue("prefix", true);
  }

}