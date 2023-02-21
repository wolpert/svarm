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

package org.svarm.common.config.healthchecks;

import static org.slf4j.LoggerFactory.getLogger;

import com.codahale.metrics.health.HealthCheck;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.accessor.EtcdAccessor;

/**
 * Validates we can still talk to Etcd.
 */
@Singleton
public class EtcdHealthCheck extends HealthCheck {

  private static final String NAMESPACE = "healthcheck";
  private static final Logger LOGGER = getLogger(EtcdHealthCheck.class);

  private final EtcdAccessor accessor;

  /**
   * Constructor.
   *
   * @param accessor to test.
   */
  @Inject
  public EtcdHealthCheck(final EtcdAccessor accessor) {
    LOGGER.info("EtcdHealthCheck({})", accessor);
    this.accessor = accessor;
  }

  @Override
  protected Result check() throws Exception {
    final String uuid = UUID.randomUUID().toString();
    LOGGER.trace("check({})", uuid);
    final String key = uuid;
    final String value = "" + System.currentTimeMillis();
    try {
      accessor.put(NAMESPACE, key, value);
      final Optional<String> result = accessor.get(NAMESPACE, key);
      if (result.isEmpty()) {
        LOGGER.warn("Missing value");
        return Result.unhealthy("No value present");
      } else if (!value.equals(result.get())) {
        LOGGER.warn("Incorrect value.");
        return Result.unhealthy("Wrong value found");
      }
    } finally {
      accessor.delete(NAMESPACE, key);
    }
    return Result.healthy();
  }
}
