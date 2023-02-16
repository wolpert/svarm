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

import static com.codeheadsystems.server.module.DropWizardModule.DROPWIZARD_APPLICATION_NAME;

import com.codeheadsystems.metrics.Metrics;
import io.micrometer.core.instrument.Tags;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used so that we can have set the default tags needed for metrics.
 */
@Singleton
public class MetricTagsResource implements ContainerRequestFilter, ContainerResponseFilter, JerseyResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricTagsResource.class);
  private final Metrics metrics;
  private final Tags defaultTags;

  /**
   * Default constructor.
   *
   * @param metrics metrics object to set the tags.
   * @param appName for creating tags.
   */
  @Inject
  public MetricTagsResource(final Metrics metrics, @Named(DROPWIZARD_APPLICATION_NAME) final String appName) {
    this.metrics = metrics;
    defaultTags = Tags.of("host", getHost(), "application", appName);
    LOGGER.info("MetricTagsResource({},{})", metrics, defaultTags);
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
    metrics.close();
    metrics.and(defaultTags.and("request", requestContext.getMethod()));
    LOGGER.trace("setting default tags: {}", defaultTags);
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
    LOGGER.trace("Clearing tags");
    metrics.close();
  }
}
