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

package org.svarm.node.module;

import com.codahale.metrics.health.HealthCheck;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import org.svarm.node.healthchecks.BasicHealthCheck;
import org.svarm.node.healthchecks.InternalDataSourceHealthCheck;
import org.svarm.node.healthchecks.TenantTablelJdbiHealthCheck;

/**
 * All health checks should be referenced here.
 */
@Module
public interface HealthCheckModule {

  /**
   * Basic health check. Filler.
   *
   * @param healthCheck health check.
   * @return health check.
   */
  @Binds
  @IntoSet
  HealthCheck basicHealthCheck(final BasicHealthCheck healthCheck);

  /**
   * Internal database health check.
   *
   * @param healthCheck health check.
   * @return health check.
   */
  @Binds
  @IntoSet
  HealthCheck internalDataSourceHealthCheck(final InternalDataSourceHealthCheck healthCheck);

  /**
   * Tenant Table database health check.
   *
   * @param healthCheck health check.
   * @return health check.
   */
  @Binds
  @IntoSet
  HealthCheck tenantTablelDataSourceHealthCheck(final TenantTablelJdbiHealthCheck healthCheck);
}
