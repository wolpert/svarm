package org.svarm.featureflag.module;

import dagger.Binds;
import dagger.Module;
import javax.inject.Singleton;
import org.svarm.featureflag.manager.FeatureLookupManager;
import org.svarm.featureflag.manager.impl.EtcdFeatureLookupManager;

/**
 * The interface Feature flag etcd module.
 */
@Module
public interface FeatureFlagEtcdModule {

  /**
   * Feature lookup manager feature lookup manager.
   *
   * @param etcdFeatureLookupManager the etcd feature lookup manager
   * @return the feature lookup manager
   */
  @Binds
  @Singleton
  FeatureLookupManager featureLookupManager(EtcdFeatureLookupManager etcdFeatureLookupManager);

}
