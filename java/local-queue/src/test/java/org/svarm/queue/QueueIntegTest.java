package org.svarm.queue;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.dropwizard.lifecycle.Managed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.svarm.queue.module.QueueModule;
import org.svarm.util.LiquibaseHelper;

/**
 * This test pretends to be a full dropwizard application.
 */
@Tag("integ")
public class QueueIntegTest {

  private static final String KEY_WORKING = "KEY_WORKING";
  public static boolean WORKING = false;
  private static QueueComponent queueComponent;

  @BeforeAll
  public static void beforeAll() {
    queueComponent = DaggerQueueIntegTest_QueueComponent.builder()
        .build();
    queueComponent.managed().forEach(managed -> {
      try {
        managed.start();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @AfterAll
  public static void afterAll() {
    queueComponent.managed().forEach(managed -> {
      try {
        managed.stop();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    Jdbi.create(queueComponent.dataSource()).withHandle(handle -> handle.execute("shutdown;"));
  }

  @Test
  public void working() {
    WORKING = false;
    queueComponent.queue().enqueue(KEY_WORKING, "payload");
    int count = 0;
    while (!WORKING && count < 20) { // wait up to 10 seconds
      try {
        Thread.sleep(500);
        count++;
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    if (!WORKING) {
      throw new RuntimeException("Not working");
    }
  }

  @Component(modules = {QueueModule.class, JdbiModule.class})
  @Singleton
  public interface QueueComponent {

    DataSource dataSource();

    Queue queue();

    Set<Managed> managed();

  }

  public static class WorkingTest implements MessageConsumer {

    @Override
    public void accept(final Message message) {
      WORKING = true;
    }
  }

  @Module
  public static class JdbiModule {

    @Provides
    @Singleton
    public Clock clock() {
      return Clock.systemUTC();
    }

    @Provides
    @IntoMap
    @StringKey(KEY_WORKING)
    public MessageConsumer workingConsumer() {
      return new WorkingTest();
    }

    // You do not have to set this if you are using the defaults.
    @Provides
    @Singleton
    public QueueConfiguration queueConfiguration() {
      return ImmutableQueueConfiguration.builder()
          .queueProcessorInitialDelay(1)
          .waitBetweenPollsSeconds(3)
          .queueProcessorInterval(3)
          .build();
    }

    // --- START METRICS ---

    /**
     * Default registry.
     *
     * @return the value.
     */
    @Provides
    @Singleton
    MeterRegistry meterRegistry() {
      return new SimpleMeterRegistry();
    }

    /**
     * The instrumented meter registry.
     *
     * @return registry. tags
     */
    @Provides
    @Singleton
    public Tags defaultTags() {
      return Tags.of("test", "true");
    }

    /**
     * Returns the supplier for the default tags.
     *
     * @param defaultTags base set.
     * @return the supplier.
     */
    @Provides
    @Singleton
    public Supplier<Tags> defaultTagSupplier(final Tags defaultTags) {
      final Optional<Tags> optional = Optional.of(defaultTags);
      return optional::get;
    }

    // ---- END METRICS ----

    @Provides
    @Singleton
    public Jdbi jdbi(final DataSource dataSource) {
      new LiquibaseHelper().runLiquibase(dataSource, "liquibase/queue.xml");
      return Jdbi.create(dataSource)
          .installPlugin(new SqlObjectPlugin());
    }

    @Provides
    @Singleton
    public DataSource dataSource() {
      final String url = "jdbc:hsqldb:mem:" + getClass().getSimpleName() + ":" + UUID.randomUUID();
      final ComboPooledDataSource cpds = new ComboPooledDataSource();
      cpds.setJdbcUrl(url);
      cpds.setUser("SA");
      cpds.setPassword("");
      cpds.setMinPoolSize(0);
      cpds.setAcquireIncrement(10);
      cpds.setMaxPoolSize(40);
      cpds.setMaxIdleTime(300);
      return cpds;
    }

  }

}
