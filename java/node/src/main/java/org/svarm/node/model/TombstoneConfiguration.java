package org.svarm.node.model;

import java.time.Duration;
import org.immutables.value.Value;

/**
 * The interface Tombstone configuration.
 */
@Value.Immutable
public interface TombstoneConfiguration {


  /**
   * Expiry duration. The time before we hard-delete the tombstones.
   *
   * @return the duration
   */
  @Value.Default
  default Duration expiryDuration() {
    return Duration.ofDays(2);
  }

  /**
   * Tombstone rerun delay duration. The delay between scheduling tombstone cleanup events.
   *
   * @return the duration
   */
  @Value.Default
  default Duration tombstoneRerunDelay() {
    return Duration.ofHours(3);
  }

  /**
   * Tombstone service start delay duration. How long to wait for the tombstone cleanup service to first schedule.
   *
   * @return the duration
   */
  @Value.Default
  default Duration tombstoneServiceStartDelay() {
    return Duration.ofSeconds(10);
  }

}
