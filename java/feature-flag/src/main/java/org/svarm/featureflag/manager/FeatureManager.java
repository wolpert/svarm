package org.svarm.featureflag.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.featureflag.Feature;
import org.svarm.featureflag.factory.FeatureFactory;

/**
 * The type Feature manager.
 */
@Singleton
public class FeatureManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

  private final FeatureFactory featureFactory;
  private final FeatureLookupManager featureLookupManager;
  private final LoadingCache<String, Feature> featureCache;

  /**
   * Instantiates a new Feature manager.
   *
   * @param featureFactory       the feature factory
   * @param featureLookupManager the feature lookup manager
   */
  public FeatureManager(final FeatureFactory featureFactory,
                        final FeatureLookupManager featureLookupManager) {
    this.featureFactory = featureFactory;
    this.featureLookupManager = featureLookupManager;
    featureCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .refreshAfterWrite(60, TimeUnit.SECONDS) // refresh from source every 60seconds
        .expireAfterAccess(6000, TimeUnit.SECONDS) // expire after 6000 seconds of inactivity
        .build(CacheLoader.asyncReloading(
            CacheLoader.from(this::lookup),
            ForkJoinPool.commonPool()));
    LOGGER.info("FeatureManager({},{})", featureLookupManager, featureFactory);
  }

  private Feature lookup(String featureId) {
    LOGGER.trace("lookup({})", featureId);
    return featureLookupManager.lookupPercentage(featureId)
        .map(featureFactory::generate)
        .orElseGet(featureFactory::disabledFeature);
  }

  /**
   * Is enabled boolean.
   *
   * @param featureId     the feature id
   * @param discriminator the discriminator
   * @return the boolean
   */
  public boolean isEnabled(String featureId, String discriminator) {
    return featureCache.getUnchecked(featureId).enabled(discriminator);
  }

}
