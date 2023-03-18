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

package org.svarm.control.module;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jdbi3.InstrumentedSqlLogger;
import dagger.Module;
import dagger.Provides;
import io.dropwizard.core.setup.Environment;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.svarm.control.ControlConfiguration;
import org.svarm.control.dao.KeyDao;
import org.svarm.control.dao.NodeDao;
import org.svarm.control.dao.NodeRangeDao;
import org.svarm.control.factory.ControlJdbiFactory;

/**
 * Provides management of the database. DAOs should be created here via the DBI.onDemand() method if possible.
 */
@Module
public class DatabaseModule {

  /**
   * The JDBI handler.
   *
   * @param environment    of dropwizard.
   * @param configuration  of the app.
   * @param factory        factory to setup.
   * @param metricRegistry for metrics.
   * @return dbi.
   */
  @Provides
  @Singleton
  public Jdbi jdbi(final ControlJdbiFactory factory,
                   final ControlConfiguration configuration,
                   final Environment environment,
                   final MetricRegistry metricRegistry) {
    return factory.generate(configuration, environment)
        .setSqlLogger(new InstrumentedSqlLogger(metricRegistry));
  }

  /**
   * Generates the KeyDao.
   *
   * @param jdbi to use.
   * @return the dao.
   */
  @Provides
  @Singleton
  public KeyDao keyDao(final Jdbi jdbi) {
    return jdbi.onDemand(KeyDao.class);
  }

  /**
   * Generates the NodeDao.
   *
   * @param jdbi to use.
   * @return the dao.
   */
  @Provides
  @Singleton
  public NodeDao nodeDao(final Jdbi jdbi) {
    return jdbi.onDemand(NodeDao.class);
  }

  /**
   * Generates the NodeRangeDao.
   *
   * @param jdbi to use.
   * @return the dao.
   */
  @Provides
  @Singleton
  public NodeRangeDao nodeRangeDao(final Jdbi jdbi) {
    return jdbi.onDemand(NodeRangeDao.class);
  }

}
