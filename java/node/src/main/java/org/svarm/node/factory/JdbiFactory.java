package org.svarm.node.factory;

import static org.slf4j.LoggerFactory.getLogger;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jdbi3.InstrumentedSqlLogger;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;

/**
 * Provides for the buildout of a JDBI interface. Sets default configuration.
 */
@Singleton
public class JdbiFactory {

  private static final Logger LOGGER = getLogger(JdbiFactory.class);

  private final MetricRegistry metricRegistry;

  /**
   * Constructor.
   *
   * @param metricRegistry for registeration.
   */
  @Inject
  public JdbiFactory(final MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
    LOGGER.info("JdbiFactory({})", metricRegistry);
  }

  /**
   * Generates the jdbi instance for the data source.
   *
   * @param dataSource to use.
   * @return the instance.
   */
  public Jdbi generate(final DataSource dataSource) {
    LOGGER.trace("generate({})", dataSource);
    final Jdbi jdbi = Jdbi.create(dataSource);
    jdbi.setSqlLogger(new InstrumentedSqlLogger(metricRegistry));
    return jdbi;
  }

}
