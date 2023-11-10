package org.svarm.queue.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.google.common.hash.Hashing;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.queue.Message;
import org.svarm.queue.State;
import org.svarm.queue.factory.MessageFactory;
import org.svarm.queue.module.QueueModule;

class MessageDaoTest {

  private final MessageFactory messageFactory = new MessageFactory(Clock.systemUTC(), Hashing.murmur3_32_fixed());
  private Jdbi jdbi;
  private DataSource dataSource;
  private MessageDao messageDao;

  @BeforeEach
  void setup() throws SQLException, LiquibaseException {
    dataSource = dataSource();
    runLiquibase(dataSource.getConnection());
    jdbi = Jdbi.create(dataSource);
    jdbi.installPlugin(new SqlObjectPlugin());
    messageDao = new QueueModule().messageDao(jdbi);
  }

  @Test
  void testRoundTrip() {
    final Message message = messageFactory.createMessage("type", "payload");
    messageDao.store(message, State.ACTIVATE);
    final Optional<Message> result = messageDao.readByUuid(message.uuid());
    assertThat(result)
        .isNotEmpty()
        .contains(message);
    messageDao.delete(message);
    assertThat(messageDao.readByUuid(message.uuid()))
        .isEmpty();
  }

  @Test
  void testHashLookup() {
    final Message message = messageFactory.createMessage("type", "payload");
    messageDao.store(message, State.ACTIVATE);
    assertThat(messageDao.readByHash(message.hash()))
        .isNotEmpty()
        .contains(message);
  }

  @Test
  void testUpdateState() {
    final Message message = messageFactory.createMessage("type", "payload");
    messageDao.store(message, State.ACTIVATE);
    assertThat(messageDao.forState(State.ACTIVATE)).containsExactly(message);
    assertThat(messageDao.stateOf(message)).contains(State.ACTIVATE);
    messageDao.updateState(message, State.PENDING);
    assertThat(messageDao.stateOf(message)).contains(State.PENDING);
    assertThat(messageDao.forState(State.PENDING)).containsExactly(message);
    assertThat(messageDao.forState(State.ACTIVATE)).isEmpty();
  }

  @Test
  void testDupPayload() throws InterruptedException {
    final Message message1 = messageFactory.createMessage("type", "payload");
    Thread.sleep(10);
    final Message message2 = messageFactory.createMessage("type", "payload");
    messageDao.store(message1, State.ACTIVATE);
    assertThatExceptionOfType(UnableToExecuteStatementException.class)
        .isThrownBy(() -> messageDao.store(message2, State.ACTIVATE))
        .withCauseInstanceOf(SQLIntegrityConstraintViolationException.class);
  }

  @Test
  void testListByState() throws InterruptedException {
    final Message message1 = messageFactory.createMessage("type", "payload:1");
    Thread.sleep(10);
    final Message message2 = messageFactory.createMessage("type", "payload:2");
    Thread.sleep(10);
    final Message message3 = messageFactory.createMessage("type", "payload:3");
    messageDao.store(message1, State.ACTIVATE);
    messageDao.store(message2, State.PENDING);
    messageDao.store(message3, State.ACTIVATE);
    final List<Message> list = messageDao.forState(State.ACTIVATE);
    assertThat(list)
        .hasSize(2)
        .containsExactly(message1, message3);
  }

  private void runLiquibase(final Connection connection) throws LiquibaseException {
    Database database = DatabaseFactory.getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
    Liquibase liquibase = new liquibase.Liquibase(
        "liquibase/queue.xml",
        new ClassLoaderResourceAccessor(),
        database);
    liquibase.update(new Contexts(), new LabelExpression());
  }

  private DataSource dataSource() {
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

  @AfterEach
  void shutdownSQLEngine() {
    Jdbi.create(dataSource).withHandle(handle -> handle.execute("shutdown;"));
  }

}