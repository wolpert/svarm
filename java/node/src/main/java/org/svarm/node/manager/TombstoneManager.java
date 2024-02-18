package org.svarm.node.manager;

import io.dropwizard.lifecycle.Managed;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.util.ShutdownUtility;
import org.svarm.node.NodeConfiguration;
import org.svarm.node.dao.TenantDao;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * The type Tombstone manager.
 */
@Singleton
public class TombstoneManager implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(TombstoneManager.class);

  private final TenantDao tenantDao;
  private final TenantTableManager tenantTableManager;
  private final TenantTableEntryManager tenantTableEntryManager;
  private final Duration tombstoneRerunDelay;
  private final Duration tombstoneServiceStartDelay;
  private final ExecutorService tombstoneService;
  private final ScheduledExecutorService scheduler;

  /**
   * Instantiates a new Tombstone manager.
   *
   * @param tenantDao               the tenant dao
   * @param tenantTableManager      the tenant table manager
   * @param tenantTableEntryManager the tenant table entry manager
   * @param nodeConfiguration       the node configuration
   */
  @Inject
  public TombstoneManager(final TenantDao tenantDao,
                          final TenantTableManager tenantTableManager,
                          final TenantTableEntryManager tenantTableEntryManager,
                          final NodeConfiguration nodeConfiguration) {
    this.tenantDao = tenantDao;
    this.tenantTableManager = tenantTableManager;
    this.tenantTableEntryManager = tenantTableEntryManager;
    this.tombstoneRerunDelay = nodeConfiguration.getTombstoneRerunDelay();
    this.tombstoneServiceStartDelay = nodeConfiguration.getTombstoneServiceStartDelay();
    this.tombstoneService = Executors.newSingleThreadExecutor();
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    LOGGER.info("TombstoneManager({},{},{},{})", tenantDao, tenantTableManager, tenantTableEntryManager, tombstoneRerunDelay);
  }


  @Override
  public void start() throws Exception {
    LOGGER.info("start()");
    scheduler.scheduleAtFixedRate(() -> {
      LOGGER.info("Finding tombstones to clean");
      tenantDao.allTenants().forEach(tenant -> {
        tenantTableManager.tables(tenant).forEach(table -> {
          final TenantTableIdentifier tenantTableIdentifier = TenantTableIdentifier.from(tenant, table);
          LOGGER.info("Scheduling tombstones cleaning for {}", tenantTableIdentifier);
          tombstoneService.execute(() -> {
            LOGGER.info("Cleaning tombstones for {}", tenantTableIdentifier);
            tenantTableEntryManager.cleanTombstones(tenantTableIdentifier);
          });
        });
      });
    }, tombstoneServiceStartDelay.toMillis(), tombstoneRerunDelay.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() throws Exception {
    LOGGER.info("stop()");
    ShutdownUtility.shutdown(LOGGER, "Tombstone service", tombstoneService);
    ShutdownUtility.shutdown(LOGGER, "Scheduler service", scheduler);
  }
}
