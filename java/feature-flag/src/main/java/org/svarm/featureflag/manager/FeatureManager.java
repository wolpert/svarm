package org.svarm.featureflag.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.featureflag.factory.Feature;
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
  @Inject
  public FeatureManager(final FeatureFactory featureFactory,
                        final FeatureLookupManager featureLookupManager) {
    this.featureFactory = featureFactory;
    this.featureLookupManager = featureLookupManager;
    featureCache = CacheBuilder.newBuilder()
        .maximumSize(100) // oh god, like we will have 100 features?
        .refreshAfterWrite(Duration.ofSeconds(60)) // refresh from source every 60seconds
        .expireAfterAccess(Duration.ofSeconds(600)) // expire after 600 seconds of inactivity
        .removalListener(notification -> LOGGER.trace("FeatureManager.removed({})", notification.getKey()))
        //.recordStats()
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

  /**
   * If enabled else t.
   *
   * @param <T>           the type parameter
   * @param featureId     the feature id
   * @param discriminator the discriminator
   * @param ifEnabled     the if enabled
   * @param ifDisabled    the if disabled
   * @return the t
   */
  public <T> T ifEnabledElse(String featureId, String discriminator, Supplier<T> ifEnabled, Supplier<T> ifDisabled) {
    return isEnabled(featureId, discriminator) ? ifEnabled.get() : ifDisabled.get();
  }

  /**
   * Invalidate.
   *
   * @param featureId the feature id
   */
  public void invalidate(String featureId) {
    featureCache.invalidate(featureId);
  }

}
