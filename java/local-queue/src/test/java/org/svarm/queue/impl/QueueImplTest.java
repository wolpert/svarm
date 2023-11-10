package org.svarm.queue.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;
import java.util.function.Consumer;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.queue.Message;
import org.svarm.queue.QueueConfiguration;
import org.svarm.queue.State;
import org.svarm.queue.dao.MessageDao;
import org.svarm.queue.factory.MessageFactory;

@ExtendWith(MockitoExtension.class)
class QueueImplTest {

  private static final String TYPE = "messageType";
  private static final String PAYLOAD = "payload";
  private static final String UUID = "uuid";
  private static final String HASH = "hash";
  @Mock private QueueRegister queueRegister;
  @Mock private MessageDao messageDao;
  @Mock private MessageFactory messageFactory;
  @Mock private QueueConfiguration queueConfiguration;

  @Mock private Message message;
  @Mock private Message lookupMessage;
  @Mock private Consumer<Message> messageConsumer;
  @Mock private UnableToExecuteStatementException unableToExecuteStatementException;
  @Mock private SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException;

  @InjectMocks private QueueImpl queue;

  @Test
  void enqueue_no_consumer() {
    assertThat(queue.enqueue(TYPE, PAYLOAD))
        .isEmpty();
  }

  @Test
  void enqueue() {
    when(queueRegister.getConsumer(TYPE)).thenReturn(Optional.of(messageConsumer));
    when(messageFactory.createMessage(TYPE, PAYLOAD)).thenReturn(message);

    assertThat(queue.enqueue(TYPE, PAYLOAD))
        .isNotEmpty()
        .contains(message);

    verify(messageDao).store(message, State.PENDING);
  }

  @Test
  void enqueue_failureToSaveMessage_configDisablesException() {
    when(queueConfiguration.exceptionOnEnqueueFail()).thenReturn(false);
    when(queueRegister.getConsumer(TYPE)).thenReturn(Optional.of(messageConsumer));
    when(messageFactory.createMessage(TYPE, PAYLOAD)).thenReturn(message);
    doThrow(unableToExecuteStatementException).when(messageDao).store(message, State.PENDING); // not dup

    assertThat(queue.enqueue(TYPE, PAYLOAD))
        .isEmpty();
  }

  @Test
  void enqueue_failureToSaveMessage_configEnablesException() {
    when(queueConfiguration.exceptionOnEnqueueFail()).thenReturn(true);
    when(queueRegister.getConsumer(TYPE)).thenReturn(Optional.of(messageConsumer));
    when(messageFactory.createMessage(TYPE, PAYLOAD)).thenReturn(message);
    doThrow(unableToExecuteStatementException).when(messageDao).store(message, State.PENDING); // not dup

    assertThatExceptionOfType(UnableToExecuteStatementException.class)
        .isThrownBy(()->queue.enqueue(TYPE, PAYLOAD));
  }

  @Test
  void enqueue_dupfailure() {
    when(queueRegister.getConsumer(TYPE)).thenReturn(Optional.of(messageConsumer));
    when(messageFactory.createMessage(TYPE, PAYLOAD)).thenReturn(message);
    when(unableToExecuteStatementException.getCause()).thenReturn(sqlIntegrityConstraintViolationException);
    doThrow(unableToExecuteStatementException).when(messageDao).store(message, State.PENDING);
    when(message.hash()).thenReturn(HASH);
    when(messageDao.readByHash(HASH)).thenReturn(Optional.of(lookupMessage));

    assertThat(queue.enqueue(TYPE, PAYLOAD))
        .isNotEmpty()
        .contains(lookupMessage);
  }

  @Test
  void enqueue_dupfailure_messageNotFound() {
    when(queueRegister.getConsumer(TYPE)).thenReturn(Optional.of(messageConsumer));
    when(messageFactory.createMessage(TYPE, PAYLOAD)).thenReturn(message);
    when(unableToExecuteStatementException.getCause()).thenReturn(sqlIntegrityConstraintViolationException);
    doThrow(unableToExecuteStatementException).when(messageDao).store(message, State.PENDING);
    when(message.hash()).thenReturn(HASH);
    when(messageDao.readByHash(HASH)).thenReturn(Optional.empty());

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(()->queue.enqueue(TYPE, PAYLOAD));
  }
}