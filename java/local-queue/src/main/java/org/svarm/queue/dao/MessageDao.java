package org.svarm.queue.dao;

import java.util.List;
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
  Optional<Message> readByUuid(@Bind("uuid") final String uuid);

  /**
   * Read by hash optional.
   *
   * @param hash the hash
   * @return the optional
   */
  @SqlQuery("select * from QUEUE where HASH = :hash")
  Optional<Message> readByHash(@Bind("hash") final String hash);

  /**
   * State of optional.
   *
   * @param message the message
   * @return the optional
   */
  @SqlQuery("select STATE from QUEUE where UUID = :uuid")
  Optional<State> stateOf(@BindPojo final Message message);

  /**
   * For state list.
   *
   * @param state the state
   * @return the list
   */
  @SqlQuery("select * from QUEUE where STATE = :state order by TIMESTAMP asc")
  List<Message> forState(@Bind("state") final State state);


  /**
   * Update state.
   *
   * @param message the message
   * @param state   the state
   */
  @SqlUpdate("update QUEUE set STATE = :state where UUID = :uuid")
  void updateState(@BindPojo final Message message, @Bind("state") final State state);

  /**
   * Delete.
   *
   * @param message the message
   */
  @SqlUpdate("delete from QUEUE where UUID = :uuid")
  void delete(@BindPojo final Message message);

  /**
   * Delete all.
   */
  @SqlUpdate("delete from QUEUE")
  void deleteAll();
}
