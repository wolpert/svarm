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

package org.svarm.common.javaclient.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.svarm.common.engine.TraceUuidEngine;

/**
 * Sets the request header for traces.
 */
@Singleton
public class TraceInterceptor implements RequestInterceptor {

  private final TraceUuidEngine traceUuidEngine;

  /**
   * Constructor.
   *
   * @param traceUuidEngine engine.
   */
  @Inject
  public TraceInterceptor(final TraceUuidEngine traceUuidEngine) {
    this.traceUuidEngine = traceUuidEngine;
  }

  @Override
  public void apply(final RequestTemplate template) {
    final String currentUuid = traceUuidEngine.get();
    final String uuid = currentUuid == null ? UUID.randomUUID().toString() : currentUuid;
    template.header(TraceUuidEngine.TRACE_UUID_HEADER, uuid);
  }
}
