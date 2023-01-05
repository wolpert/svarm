package com.codeheadsystems.dstore.node.module;

import com.codeheadsystems.dstore.node.manager.ControlPlaneManager;
import com.codeheadsystems.dstore.node.manager.FakeControlPlaneManager;
import dagger.Module;
import dagger.Provides;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import javax.inject.Singleton;

/**
 * Utilities we need internally.
 */
@Module
public class UtilitiesModule {

  /**
   * Random generator for our app.
   *
   * @return a secure random generator.
   */
  @Provides
  @Singleton
  public RandomGenerator randomGenerator() {
    return new SecureRandom();
  }

  /**
   * This will be removed once we have the real control plane.
   *
   * @param fakeControlPlaneManager fake.
   * @return fake.
   */
  @Provides
  @Singleton
  public ControlPlaneManager controlPlaneManager(final FakeControlPlaneManager fakeControlPlaneManager) {
    return fakeControlPlaneManager;
  }

}
