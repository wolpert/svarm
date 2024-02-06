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
 * The type Enablement factory.
 */
@Singleton
public class EnablementFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnablementFactory.class);

  private final HashFunction hashFunction;

  /**
   * Instantiates a new Enablement factory.
   */
  @Inject
  public EnablementFactory() {
    this(Hashing.murmur3_32_fixed());
  }

  /**
   * Instantiates a new Enablement factory.
   *
   * @param hashFunction the hash function
   */
  public EnablementFactory(HashFunction hashFunction) {
    this.hashFunction = hashFunction;
    LOGGER.info("EnablementFactory({})", hashFunction);
  }

  /**
   * Generate feature.
   *
   * @param percentage the percentage
   * @return the feature
   */
  public Enablement generate(double percentage) {
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
  public Enablement enabledFeature() {
    LOGGER.info("enabledFeature()");
    return (discriminator) -> true;
  }

  /**
   * Disabled feature feature.
   *
   * @return the feature
   */
  public Enablement disabledFeature() {
    LOGGER.info("disabledFeature()");
    return (discriminator) -> false;
  }

  /**
   * Percentage feature feature.
   *
   * @param percentage the percentage
   * @return the feature
   */
  public Enablement percentageFeature(double percentage) {
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
