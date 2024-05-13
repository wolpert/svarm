package org.svarm.node.module;

import com.codeheadsystems.metrics.Metrics;
import dagger.Module;
import dagger.Provides;
import io.etcd.jetcd.Client;
import javax.inject.Singleton;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.codeheadsystems.featureflag.manager.FeatureManager;
import org.codeheadsystems.featureflag.manager.MetricsDecorator;
import org.codeheadsystems.featureflag.manager.impl.EtcdFeatureLookupManager;

/**
 * The type Feature flag module.
 */
@Module
public class FeatureFlagModule {

  /**
   * Enablement lookup manager feature lookup manager.
   *
   * @param client the etcd client.
   * @return the feature lookup manager
   */
  @Provides
  @Singleton
  FeatureLookupManager featureLookupManager(final Client client) {
    return new EtcdFeatureLookupManager(client, "svarm");
  }

  @Provides
  @Singleton
  FeatureManager featureManager(final FeatureLookupManager featureLookupManager,
                                final Metrics metrics) {
    final MetricsDecorator decorator = new MetricsDecorator(metrics);
    return new FeatureManager.Builder()
        .withFeatureLookupManager(featureLookupManager)
        .withFeatureManagerDecorator(decorator.featureManagerDecorator())
        .withFeatureLookupManagerDecorator(decorator.featureLookupManagerDecorator())
        .build();
  }

}
