package org.svarm.featureflag.factory;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Feature factory.
 */
@Singleton
public class FeatureFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(FeatureFactory.class);

  private final HashFunction hashFunction;

  /**
   * Instantiates a new Feature factory.
   */
  @Inject
  public FeatureFactory() {
    this(Hashing.murmur3_32_fixed());
  }

  /**
   * Instantiates a new Feature factory.
   *
   * @param hashFunction the hash function
   */
  public FeatureFactory(HashFunction hashFunction) {
    this.hashFunction = hashFunction;
    LOGGER.info("FeatureFactory({})", hashFunction);
  }

  /**
   * Generate feature.
   *
   * @param percentage the percentage
   * @return the feature
   */
  public Feature generate(double percentage) {
    LOGGER.trace("generate({})", percentage);
    if (percentage <= 0.0) {
      LOGGER.trace("Disabled feature");
      return disabledFeature();
    } else if (percentage >= 1.0) {
      LOGGER.trace("Enabled feature");
      return enabledFeature();
    } else {
      LOGGER.trace("Calculated feature");
      return percentageFeature(percentage);
    }
  }

  /**
   * Enabled feature feature.
   *
   * @return the feature
   */
  public Feature enabledFeature() {
    LOGGER.info("enabledFeature()");
    return (discriminator) -> true;
  }

  /**
   * Disabled feature feature.
   *
   * @return the feature
   */
  public Feature disabledFeature() {
    LOGGER.info("disabledFeature()");
    return (discriminator) -> false;
  }

  /**
   * Percentage feature feature.
   *
   * @param percentage the percentage
   * @return the feature
   */
  public Feature percentageFeature(double percentage) {
    LOGGER.info("percentageFeature({})", percentage);
    return (discriminator) -> {
      final HashCode hashCode = hashFunction.hashString(discriminator, Charsets.UTF_8);
      final int hash = hashCode.asInt();
      final double calculated = ((double) (hash % 100)) / 100.0;
      LOGGER.trace("percentageFeature({}:{}) -> {} {}", discriminator, percentage, calculated, calculated < percentage);
      return calculated <= percentage;
    };
  }

}
