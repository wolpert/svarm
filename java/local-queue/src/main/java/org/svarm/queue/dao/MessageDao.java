package org.svarm.queue.dao;

import java.util.Optional;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.svarm.queue.Message;
import org.svarm.queue.State;

/**
 * The interface Message dao.
 */
public interface MessageDao {

  /**
   * Store.
   *
   * @param message the message
   * @param state   the state
   */
  @SqlUpdate("insert into QUEUE (UUID, TIMESTAMP, MESSAGE_TYPE, PAYLOAD, HASH, STATE) "
      + "values "
      + "(:uuid, :timestamp, :messageType, :payload, :hash, :state)")
  void store(@BindPojo final Message message, @Bind("state") final State state);

  /**
   * Read by uuid optional.
   *
   * @param uuid the uuid
   * @return the optional
   */
  @SqlQuery("select * from QUEUE where UUID = :uuid")
  Optional<Message> read_by_uuid(@Bind("uuid") final String uuid);

}
