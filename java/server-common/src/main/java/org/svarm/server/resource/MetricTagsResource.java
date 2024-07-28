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

import com.codeheadsystems.metrics.MetricFactory;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used so that we can have set the default tags needed for metrics.
 */
@Singleton
public class MetricTagsResource implements ContainerRequestFilter, ContainerResponseFilter, JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricTagsResource.class);
  private final MetricFactory metricFactory;

  private final ThreadLocal<MetricFactory.MetricsContext> metricsContextThreadLocal = new ThreadLocal<>();

  /**
   * Default constructor.
   *
   * @param metricFactory metrics object to set the tags.
   */
  @Inject
  public MetricTagsResource(final MetricFactory metricFactory) {
    this.metricFactory = metricFactory;
    LOGGER.info("MetricTagsResource({})", metricFactory);
  }

  private static String getHost() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Sets the default tags.
   *
   * @param requestContext request context.
   * @throws IOException if anything goes wrong.
   */
  @Override
  public void filter(final ContainerRequestContext requestContext) throws IOException {
    final MetricFactory.MetricsContext oldContext = metricsContextThreadLocal.get();
    if (oldContext != null) {
      LOGGER.warn("Metrics context already set. This is a bug. {}", oldContext);
      metricFactory.disableMetricsContext(oldContext);
    }
    final MetricFactory.MetricsContext context = metricFactory.enableMetricsContext();
    metricsContextThreadLocal.set(context);
    //TODO: this is bad when tenant/table or whatnot appears in the path. Figure it out. :/
    metricFactory.and("path", requestContext.getUriInfo().getPath());
  }

  /**
   * Clears all tags at this point.
   *
   * @param requestContext  request context.
   * @param responseContext response context.
   * @throws IOException if anything goes wrong.
   */
  @Override
  public void filter(final ContainerRequestContext requestContext,
                     final ContainerResponseContext responseContext) throws IOException {
    final MetricFactory.MetricsContext context = metricsContextThreadLocal.get();
    if (context == null) {
      LOGGER.warn("Metrics context not set. This is a bug.");
    } else {
      metricFactory.disableMetricsContext(context);
      metricsContextThreadLocal.remove();
    }
  }
}
