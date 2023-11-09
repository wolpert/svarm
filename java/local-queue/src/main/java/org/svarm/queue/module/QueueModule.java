package org.svarm.queue.module;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;
import org.svarm.queue.Message;
import org.svarm.queue.dao.MessageDao;

/**
 * The type Queue module.
 */
@Module
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

}
