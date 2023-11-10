package org.svarm.queue.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.queue.Message;

@ExtendWith(MockitoExtension.class)
class QueueRegisterTest {

  private static final String TYPE = "type";
  @Mock private Consumer<Message> consumer;
  @Mock private Message message;
  @InjectMocks private QueueRegister queueRegister;

  @Test
  void testRegister() {
    queueRegister.register(TYPE, consumer);
    assertThat(queueRegister.getRegisteredMessageTypes()).containsExactly(TYPE);
  }

  @Test
  void testDeregister() {
    queueRegister.register(TYPE, consumer);
    assertThat(queueRegister.getRegisteredMessageTypes()).containsExactly(TYPE);
    queueRegister.deregister(TYPE);
    assertThat(queueRegister.getRegisteredMessageTypes()).isEmpty();
  }

  @Test
  void testGetConsumer() {
    queueRegister.register(TYPE, consumer);
    assertThat(queueRegister.getConsumer(TYPE)).isPresent();
    queueRegister.deregister(TYPE);
    assertThat(queueRegister.getConsumer(TYPE)).isEmpty();
  }

  @Test
  void testDeregisterAll() {
    queueRegister.register(TYPE, consumer);
    assertThat(queueRegister.getRegisteredMessageTypes()).containsExactly(TYPE);
    queueRegister.deregisterAll();
    assertThat(queueRegister.getRegisteredMessageTypes()).isEmpty();
  }
}