package org.svarm.server.initializer;

import static org.slf4j.LoggerFactory.getLogger;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.core.setup.Environment;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Initializes all health checks.
 */
@Singleton
public class HealthCheckInitializer implements Initializer {

  private static final Logger LOGGER = getLogger(HealthCheckInitializer.class);

  private final Set<HealthCheck> healthChecks;

  /**
   * Constructor.
   *
   * @param healthChecks to initialize.
   */
  @Inject
  public HealthCheckInitializer(final Set<HealthCheck> healthChecks) {
    LOGGER.trace("HealthCheckInitializer({})", healthChecks);
    this.healthChecks = healthChecks;
  }


  @Override
  public void initialize(final Environment environment) {
    LOGGER.info("\n---\n--- Registering Health Checks ---\n---");
    for (HealthCheck healthCheck : healthChecks) {
      LOGGER.info("Registering healthCheck: {}", healthCheck.getClass().getSimpleName());
      environment.healthChecks().register(healthCheck.getClass().getSimpleName(), healthCheck);
    }
  }
}
