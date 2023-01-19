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

package com.codeheadsystems.server.resource;

import java.io.IOException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Used so that we can have request/responses use the traceUUID concept for request tracing.
 */
@Singleton
public class TraceUuid implements ContainerRequestFilter, ContainerResponseFilter, JerseyResource {

  /**
   * Identifier for the header.
   */
  public static final String TRACE_UUID_HEADER = "X-TraceUuid";
  private static final String MDC_ID = "trace";

  private static final Logger LOGGER = LoggerFactory.getLogger(TraceUuid.class);

  private final ThreadLocal<String> traceThreadLocal = new ThreadLocal<>();

  /**
   * Default constructor.
   */
  @Inject
  public TraceUuid() {
    LOGGER.info("TraceUuid");
  }

  private static String getOrCreatedUuid(final String uuid) {
    return uuid == null ? UUID.randomUUID().toString() : uuid;
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
   * Gets the UUID from the header, and sets it in the thread local. If there is none, create it.
   *
   * @param requestContext request context.
   * @throws IOException if anything goes wrong.
   */
  @Override
  public void filter(final ContainerRequestContext requestContext) throws IOException {
    final String uuid = requestContext.getHeaderString(TRACE_UUID_HEADER);
    MDC.put(MDC_ID, uuid);
    LOGGER.info("filter(request):{}", uuid);
    traceThreadLocal.set(getOrCreatedUuid(uuid));
  }

  /**
   * Sets the current trace id in the response. Removes it from the header.
   *
   * @param requestContext  request context.
   * @param responseContext response context.
   * @throws IOException if anything goes wrong.
   */
  @Override
  public void filter(final ContainerRequestContext requestContext,
                     final ContainerResponseContext responseContext) throws IOException {
    final String uuid = get();
    LOGGER.info("filter(response):{}", uuid);
    responseContext.getHeaders().add(TRACE_UUID_HEADER, getOrCreatedUuid(uuid));
    traceThreadLocal.set(null);
    MDC.remove(MDC_ID);
  }
}
