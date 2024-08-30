package org.svarm.queue.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.queue.Message;

@ExtendWith(MockitoExtension.class)
class MessageFactoryTest {

  @Mock private Clock clock;
  private MessageFactory messageFactory;

  @BeforeEach
  void setup() {
    messageFactory = new MessageFactory(clock);
  }

  @Test
  void testCreateMessage() {
    when(clock.instant()).thenReturn(java.time.Instant.EPOCH);
    Message message = messageFactory.createMessage("type", "payload");
    assertThat(message).isNotNull();
    assertThat(message.hash()).isNotNull();
    assertThat(message.timestamp()).isEqualTo(java.time.Instant.EPOCH.toEpochMilli());
    assertThat(message.messageType()).isEqualTo("type");
    assertThat(message.payload()).isEqualTo("payload");
  }

  @Test
  void testHashChanges() {
    when(clock.instant()).thenReturn(java.time.Instant.EPOCH);
    Message message1 = messageFactory.createMessage("type", "payload1");
    Message message2 = messageFactory.createMessage("type", "payload2");
    assertThat(message1.hash()).isNotEqualTo(message2.hash());
  }

}