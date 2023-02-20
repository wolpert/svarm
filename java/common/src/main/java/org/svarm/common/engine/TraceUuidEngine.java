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

package org.svarm.common.engine;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Provide a common trace platform. This just connects client requests to server requests. This minor piece of code
 * is here to unify the variables so it can be used in multiple ways.
 */
@Singleton
public class TraceUuidEngine {

  /**
   * Identifier for the header. Usable by clients and servers alike.
   */
  public static final String TRACE_UUID_HEADER = "X-TraceUuid";
  private static final String MDC_ID = "trace";

  private static final Logger LOGGER = LoggerFactory.getLogger(TraceUuidEngine.class);

  private final ThreadLocal<String> traceThreadLocal = new ThreadLocal<>();

  /**
   * Constructor.
   */
  @Inject
  public TraceUuidEngine() {
    LOGGER.info("TraceUuidEngine");
  }

  /**
   * Gets the current ID, if set. Can be null.
   *
   * @return String UUID.
   */
  public String get() {
    return traceThreadLocal.get();
  }

  /**
   * Clears the trace id.
   */
  public void clear() {
    MDC.remove(MDC_ID);
    traceThreadLocal.set(null);
  }

  /**
   * Gets the current ID, if set. Can be null.
   *
   * @param uuid for the trace.
   */
  public void set(final String uuid) {
    traceThreadLocal.set(uuid);
    MDC.put(MDC_ID, uuid);
  }
}
