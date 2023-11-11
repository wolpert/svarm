package org.svarm.queue.module;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import dagger.multibindings.Multibinds;
import io.dropwizard.lifecycle.Managed;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;
import org.svarm.queue.Message;
import org.svarm.queue.MessageConsumer;
import org.svarm.queue.QueueConfiguration;
import org.svarm.queue.dao.MessageDao;
import org.svarm.queue.factory.QueueConfigurationFactory;
import org.svarm.queue.impl.MessageConsumerExecutor;
import org.svarm.queue.impl.QueueProcessor;

/**
 * The type Queue module.
 */
@Module(includes = QueueModule.Binder.class)
public class QueueModule {

  /**
   * The constant QUEUE_PROCESSOR_SCHEDULER.
   */
  public static final String QUEUE_PROCESSOR_SCHEDULER = "QueueProcessorScheduler";
  /**
   * The constant QUEUE_PROCESSOR_EXECUTOR.
   */
  public static final String QUEUE_PROCESSOR_EXECUTOR = "QueueProcessorExecutor";

  /**
   * Message dao message dao.
   *
   * @param jdbi the jdbi, which we require already has the SQLObjects and immutable plugin installed.
   * @return the message dao
   */
  @Singleton
  @Provides
  public MessageDao messageDao(final Jdbi jdbi) {
    jdbi.getConfig(JdbiImmutables.class)
        .registerImmutable(Message.class);
    return jdbi.onDemand(MessageDao.class);
  }

  /**
   * Scheduled executor service scheduled executor service.
   *
   * @return the scheduled executor service
   */
  @Singleton
  @Provides
  @Named(QUEUE_PROCESSOR_SCHEDULER)
  public ScheduledExecutorService scheduledExecutorService() {
    return Executors.newScheduledThreadPool(1);
  }

  /**
   * Thread pool executor executor service.
   *
   * @param factory the factory
   * @return the executor service
   */
  @Singleton
  @Provides
  @Named(QUEUE_PROCESSOR_EXECUTOR)
  public ExecutorService threadPoolExecutor(final QueueConfigurationFactory factory) {
    final QueueConfiguration configuration = factory.queueConfiguration();
    return new ThreadPoolExecutor(configuration.queueExecutorMinThreads(),
        configuration.queueExecutorMaxThreads(),
        configuration.queueExecutorIdleSeconds(),
        TimeUnit.SECONDS, new LinkedBlockingQueue<>());
  }

  /**
   * The interface Binder.
   */
  @Module
  interface Binder {


    /**
     * Queue configuration queue configuration. If you don't define one, we use the default.
     *
     * @return the queue configuration
     */
    @BindsOptionalOf
    QueueConfiguration queueConfiguration();

    /**
     * Managed instance of the queue processor for the runtimes.
     *
     * @param queueProcessor the queue processor
     * @return the managed
     */
    @IntoSet
    @Binds
    Managed managedQueueProcessor(final QueueProcessor queueProcessor);

    /**
     * Managed message consumer executor managed.
     *
     * @param messageConsumerExecutor the message consumer executor
     * @return the managed
     */
    @IntoSet
    @Binds
    Managed managedMessageConsumerExecutor(final MessageConsumerExecutor messageConsumerExecutor);

    /**
     * Message consumers map.
     *
     * @return the map
     */
    @Multibinds
    Map<String, MessageConsumer> messageConsumers();

  }

}
