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

package org.svarm.common.config.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.accessor.EtcdAccessor;

/**
 * TODO: this class uses thread.sleep() before a verify. This should be replaced with a proper callback.
 */
@ExtendWith(MockitoExtension.class)
class WatchEngineTest extends BaseMetricTest {

  private static final String NAMESPACE = "namespace";
  private static final String KEY = "key";
  private static final String VALUE = "Value";

  @Mock private EtcdAccessor accessor;
  @Mock private Consumer<Event> eventConsumer;
  @Mock private WatchResponse watchResponse;
  @Mock private WatchEvent watchEvent;
  @Mock private KeyValue keyValue;
  @Mock private Watch.Watcher watcher;
  @Captor private ArgumentCaptor<Event> eventArgumentCaptor;
  @Captor private ArgumentCaptor<Watch.Listener> listenerArgumentCaptor;

  private WatchEngine watchEngine;

  @BeforeEach
  void setupEngine() {
    when(accessor.watch(eq(NAMESPACE), eq(KEY), listenerArgumentCaptor.capture())).thenReturn(watcher);
    watchEngine = new WatchEngine(accessor, metrics, NAMESPACE, KEY, eventConsumer);
  }

  @AfterEach
  void shutdown() {
    watchEngine.close();
    verify(watcher).close();
  }

  @Test
  void complete() {
    watchEngine.complete(); // no error
  }

  @Test
  void error() {
    watchEngine.error(new RuntimeException()); // no error
  }

  @Test
  void watchResponse_put() throws InterruptedException {
    when(keyValue.getKey()).thenReturn(ByteSequence.from(KEY.getBytes()));
    when(keyValue.getValue()).thenReturn(ByteSequence.from(VALUE.getBytes()));
    when(watchEvent.getKeyValue()).thenReturn(keyValue);
    when(watchEvent.getEventType()).thenReturn(WatchEvent.EventType.PUT);
    when(watchResponse.getEvents()).thenReturn(List.of(watchEvent));
    watchEngine.watchResponse(watchResponse);
    retry(5, () -> assertThat(watchEngine.queueSize()).isEqualTo(0));
    Thread.sleep(200);
    verify(eventConsumer).accept(eventArgumentCaptor.capture());
    assertThat(eventArgumentCaptor.getValue())
        .isNotNull()
        .hasFieldOrPropertyWithValue("key", KEY)
        .hasFieldOrPropertyWithValue("value", VALUE)
        .hasFieldOrPropertyWithValue("type", Event.Type.PUT);
  }

  @Test
  void watchResponse_delete() throws InterruptedException {
    when(keyValue.getKey()).thenReturn(ByteSequence.from(KEY.getBytes()));
    when(keyValue.getValue()).thenReturn(ByteSequence.from(VALUE.getBytes()));
    when(watchEvent.getKeyValue()).thenReturn(keyValue);
    when(watchEvent.getEventType()).thenReturn(WatchEvent.EventType.DELETE);
    when(watchResponse.getEvents()).thenReturn(List.of(watchEvent));
    watchEngine.watchResponse(watchResponse);
    retry(5, () -> assertThat(watchEngine.queueSize()).isEqualTo(0));
    Thread.sleep(200);
    verify(eventConsumer).accept(eventArgumentCaptor.capture());
    assertThat(eventArgumentCaptor.getValue())
        .isNotNull()
        .hasFieldOrPropertyWithValue("key", KEY)
        .hasFieldOrPropertyWithValue("value", VALUE)
        .hasFieldOrPropertyWithValue("type", Event.Type.DELETE);
  }

  @Test
  void watchResponse_unknown() {
    when(watchEvent.getEventType()).thenReturn(WatchEvent.EventType.UNRECOGNIZED);
    when(watchResponse.getEvents()).thenReturn(List.of(watchEvent));
    watchEngine.watchResponse(watchResponse);
    retry(5, () -> assertThat(watchEngine.queueSize()).isEqualTo(0));
    verifyNoInteractions(eventConsumer);
  }

  private void retry(final int times, final Runnable runnable) {
    AssertionError error = null;
    for (int attempt = 0; attempt < times; attempt++) {
      try {
        runnable.run();
        return;
      } catch (AssertionError ae) {
        error = ae;
        System.out.println("Failed, attempt " + attempt);
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    if (error != null) {
      throw error;
    } else {
      throw new IllegalStateException("Should have caught an error");
    }
  }

}