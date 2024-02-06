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
import org.svarm.featureflag.factory.Enablement;
import org.svarm.featureflag.factory.EnablementFactory;

/**
 * The type Enablement manager.
 */
@Singleton
public class FeatureManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

  private final EnablementFactory enablementFactory;
  private final FeatureLookupManager featureLookupManager;
  private final LoadingCache<String, Enablement> featureEnablementCache;

  /**
   * Instantiates a new Feature manager.
   *
   * @param enablementFactory       the feature factory
   * @param featureLookupManager the feature lookup manager
   */
  @Inject
  public FeatureManager(final EnablementFactory enablementFactory,
                        final FeatureLookupManager featureLookupManager) {
    this.enablementFactory = enablementFactory;
    this.featureLookupManager = featureLookupManager;
    this.featureEnablementCache = CacheBuilder.newBuilder()
        .maximumSize(100) // oh god, like we will have 100 features?
        .refreshAfterWrite(Duration.ofSeconds(60)) // refresh from source every 60seconds
        .expireAfterAccess(Duration.ofSeconds(600)) // expire after 600 seconds of inactivity
        .removalListener(notification -> LOGGER.trace("removalListener({})", notification.getKey()))
        //.recordStats()
        .build(CacheLoader.asyncReloading(
            CacheLoader.from(this::lookup),
            ForkJoinPool.commonPool()));
    LOGGER.info("FeatureManager({},{})", featureLookupManager, enablementFactory);
  }

  private Enablement lookup(String featureId) {
    LOGGER.info("lookup({})", featureId);
    return featureLookupManager.lookupPercentage(featureId)
        .map(enablementFactory::generate)
        .orElseGet(enablementFactory::disabledFeature);
  }

  /**
   * Is enabled boolean.
   *
   * @param featureId     the feature id
   * @param discriminator the discriminator
   * @return the boolean
   */
  public boolean isEnabled(String featureId, String discriminator) {
    return featureEnablementCache.getUnchecked(featureId).enabled(discriminator);
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
   * Invalidate the feature id in the cache.
   *
   * @param featureId the feature id
   */
  public void invalidate(String featureId) {
    featureEnablementCache.invalidate(featureId);
  }

}
