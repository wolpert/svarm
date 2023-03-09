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

package org.svarm.control.resource;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.svarm.control.exception.NotEnoughNodesException;
import org.svarm.server.exception.NotFoundException;
import org.svarm.server.resource.JerseyResource;

/**
 * Mapper for NotFoundExceptions.
 */
@Singleton
public class NotEnoughNodesExceptionMapper implements JerseyResource, ExceptionMapper<NotEnoughNodesException> {

  private final Meter exceptions;

  /**
   * Constructor.
   *
   * @param registry doing this the dropwizard way.
   */
  @Inject
  public NotEnoughNodesExceptionMapper(final MetricRegistry registry) {
    exceptions = registry.meter(name(getClass(), "exceptions"));
  }

  /**
   * Convert the exception to a response.
   *
   * @param exception the exception to map to a response.
   * @return the 500 response.
   */
  @Override
  public Response toResponse(final NotEnoughNodesException exception) {
    exceptions.mark();
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity("Not enough nodes for new table")
        .build();
  }
}
