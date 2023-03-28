package org.svarm.server.initializer;

import static org.slf4j.LoggerFactory.getLogger;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Initializes all Managed objects.
 */
@Singleton
public class ManagedObjectInitializer implements Initializer {

  private static final Logger LOGGER = getLogger(ManagedObjectInitializer.class);

  private final Set<Managed> managedSet;
  private final Environment environment;

  /**
   * Constructor.
   *
   * @param managedSet  to initialize.
   * @param environment from dropwizard.
   */
  @Inject
  public ManagedObjectInitializer(final Set<Managed> managedSet,
                                  final Environment environment) {
    LOGGER.trace("ManagedObjectInitializer({})", managedSet);
    this.environment = environment;
    this.managedSet = managedSet;
  }


  @Override
  public void initialize() {
    LOGGER.info("\n---\n--- Registering Managed Objects ---\n---");
    for (Managed managed : managedSet) {
      LOGGER.info("Registering managed object: {}", managed.getClass().getSimpleName());
      environment.lifecycle().manage(managed);
    }
  }
}
