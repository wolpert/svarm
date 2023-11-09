package org.svarm.queue.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.hash.Hashing;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
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
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.queue.Message;
import org.svarm.queue.State;
import org.svarm.queue.factory.MessageFactory;
import org.svarm.queue.module.QueueModule;

class MessageDaoTest {

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
  void testRoundTrip(){
    final Message message = new MessageFactory(Clock.systemUTC(), Hashing.murmur3_32_fixed())
        .createMessage("type", "payload");
    messageDao.store(message, State.ACTIVATE);
    final Optional<Message> result = messageDao.read_by_uuid(message.uuid());
    assertThat(result)
        .isNotEmpty()
        .contains(message);
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