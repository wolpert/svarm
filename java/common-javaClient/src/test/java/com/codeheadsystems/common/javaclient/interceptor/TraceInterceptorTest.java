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

package com.codeheadsystems.common.javaclient.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.common.engine.TraceUuidEngine;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraceInterceptorTest {

  private static final String UUID = "uuid";
  @Mock private TraceUuidEngine engine;
  @Mock private RequestTemplate requestTemplate;
  @Captor private ArgumentCaptor<String> argumentCaptor;

  @InjectMocks private TraceInterceptor interceptor;

  @Test
  void apply_noUuid() {
    interceptor.apply(requestTemplate);

    verify(requestTemplate).header(eq(TraceUuidEngine.TRACE_UUID_HEADER), argumentCaptor.capture());

    assertThat(argumentCaptor.getValue()).isNotNull();
  }

  @Test
  void apply_withUuid() {
    when(engine.get()).thenReturn(UUID);
    interceptor.apply(requestTemplate);

    verify(requestTemplate).header(eq(TraceUuidEngine.TRACE_UUID_HEADER), argumentCaptor.capture());

    assertThat(argumentCaptor.getValue()).isNotNull().isEqualTo(UUID);
  }

}