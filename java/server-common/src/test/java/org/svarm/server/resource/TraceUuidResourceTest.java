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

package org.svarm.server.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.svarm.common.engine.TraceUuidEngine.TRACE_UUID_HEADER;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.engine.TraceUuidEngine;

@ExtendWith(MockitoExtension.class)
class TraceUuidResourceTest {

  private static final String UUID = "UUID";
  @Mock private ContainerRequestContext requestContext;
  @Mock private ContainerResponseContext responseContext;
  @Mock private MultivaluedMap<String, Object> multivaluedMap;

  private TraceUuidEngine traceUuidEngine;
  private TraceUuidResource traceUUIDResource;

  @BeforeEach
  void setup() {
    traceUuidEngine = new TraceUuidEngine();
    traceUUIDResource = new TraceUuidResource(traceUuidEngine);
  }

  @AfterEach
  void tearDown() {
    traceUuidEngine.clear();
  }

  @Test
  public void roundTrip_notPreSet() throws IOException {
    when(responseContext.getHeaders()).thenReturn(multivaluedMap);

    assertThat(traceUuidEngine.get()).isNull();

    traceUUIDResource.filter(requestContext);
    final String uuid = traceUuidEngine.get();
    assertThat(uuid).isNotNull();

    traceUUIDResource.filter(requestContext, responseContext);
    assertThat(traceUuidEngine.get()).isNull();
    verify(multivaluedMap).add(TRACE_UUID_HEADER, uuid);
  }

  @Test
  public void roundTrip_preSet() throws IOException {
    when(responseContext.getHeaders()).thenReturn(multivaluedMap);
    when(requestContext.getHeaderString(TRACE_UUID_HEADER)).thenReturn(UUID);

    assertThat(traceUuidEngine.get()).isNull();

    traceUUIDResource.filter(requestContext);
    final String uuid = traceUuidEngine.get();
    assertThat(uuid).isNotNull().isEqualTo(UUID);

    traceUUIDResource.filter(requestContext, responseContext);
    assertThat(traceUuidEngine.get()).isNull();
    verify(multivaluedMap).add(TRACE_UUID_HEADER, uuid);
  }

}