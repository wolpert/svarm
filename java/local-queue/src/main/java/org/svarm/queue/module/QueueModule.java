package org.svarm.queue.module;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import dagger.multibindings.Multibinds;
import io.dropwizard.lifecycle.Managed;
import java.util.Map;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;
import org.svarm.queue.Message;
import org.svarm.queue.MessageConsumer;
import org.svarm.queue.QueueConfiguration;
import org.svarm.queue.dao.MessageDao;
import org.svarm.queue.impl.QueueProcessor;

/**
 * The type Queue module.
 */
@Module(includes = QueueModule.Binder.class)
public class QueueModule {

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
    Managed managed(final QueueProcessor queueProcessor);

    /**
     * Message consumers map.
     *
     * @return the map
     */
    @Multibinds
    Map<String, MessageConsumer> messageConsumers();

  }

}
