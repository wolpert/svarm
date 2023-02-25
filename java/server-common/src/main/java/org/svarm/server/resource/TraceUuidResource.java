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

import static org.svarm.common.engine.TraceUuidEngine.TRACE_UUID_HEADER;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.engine.TraceUuidEngine;

/**
 * Used so that we can have request/responses use the traceUUID concept for request tracing. Ensures the UUID
 * is reset at the end of the request.
 */
@Singleton
public class TraceUuidResource implements ContainerRequestFilter, ContainerResponseFilter, JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TraceUuidResource.class);
  private final TraceUuidEngine traceUuidEngine;

  /**
   * Default constructor.
   *
   * @param traceUuidEngine engine.
   */
  @Inject
  public TraceUuidResource(final TraceUuidEngine traceUuidEngine) {
    this.traceUuidEngine = traceUuidEngine;
    LOGGER.info("TraceUuidResource");
  }

  private static String getOrCreatedUuid(final String uuid) {
    return uuid == null ? UUID.randomUUID().toString() : uuid;
  }

  /**
   * Gets the UUID from the header, and sets it in the thread local. If there is none, create it.
   *
   * @param requestContext request context.
   * @throws IOException if anything goes wrong.
   */
  @Override
  public void filter(final ContainerRequestContext requestContext) throws IOException {
    final String uuidFromHeader = requestContext.getHeaderString(TRACE_UUID_HEADER);
    final boolean fromClient = uuidFromHeader != null; // the client set it.
    final String uuid = getOrCreatedUuid(uuidFromHeader);
    LOGGER.trace("filter(client_set:{}):{}", fromClient, uuid);
    traceUuidEngine.set(uuid);
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
    final String uuid = traceUuidEngine.get();
    LOGGER.trace("filter(response):{}", uuid);
    responseContext.getHeaders().add(TRACE_UUID_HEADER, getOrCreatedUuid(uuid));
    traceUuidEngine.clear();
  }
}
