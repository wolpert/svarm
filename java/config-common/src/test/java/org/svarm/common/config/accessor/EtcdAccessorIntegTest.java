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

import com.codeheadsystems.metrics.test.BaseMetricTest;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.test.EtcdClusterExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("integ")
class EtcdAccessorIntegTest extends BaseMetricTest {
  @RegisterExtension
  public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
      .withNodes(1)
      .build();
  private static final String KEY = "key";
  private static final String VALUE = "value";
  private static final String NAMESPACE = "namespace";
  protected Client client;
  protected EtcdAccessor accessor;

  @BeforeEach
  void setupClient() {
    client = Client.builder().endpoints(cluster.clientEndpoints()).build();
    accessor = new EtcdAccessor(client, "test", metrics);
  }

  @AfterEach
  void tearDownClient() {
    client.close();
  }

  @Test
  void roundTrip() {
    assertThat(accessor.get(NAMESPACE, KEY))
        .isNotPresent();

    accessor.put(NAMESPACE, KEY, VALUE);
    assertThat(accessor.get(NAMESPACE, KEY))
        .isPresent()
        .contains(VALUE);

    accessor.delete(NAMESPACE, KEY);
    assertThat(accessor.get(NAMESPACE, KEY))
        .isNotPresent();
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