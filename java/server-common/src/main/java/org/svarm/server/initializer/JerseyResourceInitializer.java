package org.svarm.server.initializer;

import static org.slf4j.LoggerFactory.getLogger;

import io.dropwizard.core.setup.Environment;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.server.resource.JerseyResource;

/**
 * Initializes all Jersey Resources.
 */
@Singleton
public class JerseyResourceInitializer implements Initializer {

  private static final Logger LOGGER = getLogger(JerseyResourceInitializer.class);

  private final Set<JerseyResource> jerseyResources;
  private final Environment environment;

  /**
   * Constructor.
   *
   * @param jerseyResources to initialize.
   * @param environment     from dropwizard.
   */
  @Inject
  public JerseyResourceInitializer(final Set<JerseyResource> jerseyResources,
                                   final Environment environment) {
    LOGGER.trace("JerseyResourceInitializer({})", jerseyResources);
    this.environment = environment;
    this.jerseyResources = jerseyResources;
  }


  @Override
  public void initialize() {
    LOGGER.info("\n---\n--- Registering Resources ---\n---");
    for (Object resource : jerseyResources) {
      LOGGER.info("Registering resource: {}", resource.getClass().getSimpleName());
      environment.jersey().register(resource);
    }
  }
}
