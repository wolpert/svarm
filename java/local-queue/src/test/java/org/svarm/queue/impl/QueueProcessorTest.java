package org.svarm.queue.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.queue.Message;
import org.svarm.queue.QueueConfiguration;
import org.svarm.queue.State;
import org.svarm.queue.dao.MessageDao;
import org.svarm.queue.factory.QueueConfigurationFactory;

@ExtendWith(MockitoExtension.class)
class QueueProcessorTest {

  @Mock private MessageDao messageDao;
  @Mock private QueueConfiguration queueConfiguration;
  @Mock private MessageConsumerExecutor messageConsumerExecutor;
  @Mock private ScheduledExecutorService scheduledExecutorService;
  @Mock private Message message;

  @Mock private ScheduledFuture scheduler;

  private QueueProcessor processor;

  @BeforeEach
  void setup() {
    processor = new QueueProcessor(messageDao, new QueueConfigurationFactory(Optional.of(queueConfiguration)), messageConsumerExecutor, scheduledExecutorService);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testStart() {
    when(messageDao.forState(State.ACTIVATING)).thenReturn(List.of(message));
    when(messageDao.forState(State.PROCESSING)).thenReturn(List.of());
    when(queueConfiguration.queueProcessorInitialDelay()).thenReturn(1);
    when(queueConfiguration.queueProcessorInterval()).thenReturn(0);
    when(scheduledExecutorService.scheduleAtFixedRate(any(), eq(1L), eq(0L), eq(TimeUnit.SECONDS)))
        .thenReturn(scheduler);

    processor.start();
    processor.start();

    verify(messageDao, times(1)).updateState(message, State.PENDING);
  }

  @Test
  void testProcessingPendingQueue(){
    when(messageDao.forState(State.PENDING)).thenReturn(List.of(message));

    processor.processPendingQueue();

    verify(messageDao, times(1)).updateState(message, State.ACTIVATING);
    verify(messageConsumerExecutor, times(1)).enqueue(message);
  }

}