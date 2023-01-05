/*
 * Copyright (c) 2022-2023. Ned Wolpert
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

package com.codeheadsystems.dstore.node.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample healthcheck. Should be replaced by something useful.
 */
@Singleton
public class BasicHealthCheck extends HealthCheck {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicHealthCheck.class);

  /**
   * Default constructor.
   */
  @Inject
  public BasicHealthCheck() {
    LOGGER.info("BasicHealthCheck()");
  }

  /**
   * Default health check.
   *
   * @return that everything is healthy.
   * @throws Exception if something went wrong.
   */
  @Override
  protected Result check() throws Exception {
    LOGGER.debug("check()");
    return Result.healthy();
  }
}
