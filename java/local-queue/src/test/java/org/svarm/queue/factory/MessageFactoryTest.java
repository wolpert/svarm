package org.svarm.queue.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
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
  private final HashFunction hashFunction = Hashing.murmur3_32_fixed();
  private MessageFactory messageFactory;

  @BeforeEach
  void setup() {
    messageFactory = new MessageFactory(clock, hashFunction);
  }

  @Test
  void testCreateMessage() {
    when(clock.instant()).thenReturn(java.time.Instant.EPOCH);
    Message message = messageFactory.createMessage("type", "payload");
    assertThat(message).isNotNull();
    assertThat(message.uuid()).isNotNull();
    assertThat(message.timestamp()).isEqualTo(java.time.Instant.EPOCH);
    assertThat(message.messageType()).isEqualTo("type");
    assertThat(message.payload()).isEqualTo("payload");
  }

}