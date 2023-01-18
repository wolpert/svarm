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

package com.codeheadsystems.dstore.node.factory;

import com.codeheadsystems.dstore.node.model.TenantTable;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides instances of the datasource itself. To be used by the datasource manager.
 * This will make it easy to change/configure the data sources as needed.
 */
@Singleton
public class DataSourceFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);

  /**
   * Constructor.
   */
  @Inject
  public DataSourceFactory() {
    LOGGER.info("DataSourceFactory()");
  }

  /**
   * Returns the data source for the tenant table. Not cached.
   *
   * @param table we want.
   * @param url   the url we have.
   * @return the data source.
   */
  public DataSource tenantDataSource(final TenantTable table, final String url) {
    LOGGER.trace("tenantDataSource({})", table);
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(url);
    cpds.setUser("SA");
    cpds.setPassword("");
    cpds.setMinPoolSize(0);
    cpds.setAcquireIncrement(10);
    cpds.setMaxPoolSize(40);
    cpds.setMaxIdleTime(300);
    //cpds.setTestConnectionOnCheckout(true);
    return cpds;
  }

  /**
   * Returns the internal data source. Not cached.
   *
   * @param url we want to use.
   * @return the data source.
   */
  public DataSource internalDataSource(final String url) {
    LOGGER.trace("internalDataSource()");
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    cpds.setJdbcUrl(url);
    cpds.setUser("SA");
    cpds.setPassword("");
    cpds.setMinPoolSize(1);
    cpds.setAcquireIncrement(5);
    cpds.setMaxPoolSize(20);
    cpds.setMaxIdleTime(300);
    //cpds.setTestConnectionOnCheckout(true);
    return cpds;
  }

}
