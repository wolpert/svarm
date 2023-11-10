package org.svarm.queue.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.queue.MessageConsumer;

/**
 * The type Queue register.
 */
@Singleton
public class QueueRegister {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueueRegister.class);
  private final Map<String, MessageConsumer> consumerMap;

  /**
   * Instantiates a new Queue register.
   *
   * @param map the map
   */
  @Inject
  public QueueRegister(final Map<String, MessageConsumer> map) {
    LOGGER.info("QueueRegister()");
    if (map == null) {
      consumerMap = new HashMap<>();
    } else {
      consumerMap = new HashMap<>(map);
    }
  }

  /**
   * Register.
   *
   * @param messageType the message type
   * @param consumer    the consumer
   */
  public void register(final String messageType,
                       final MessageConsumer consumer) {
    LOGGER.trace("register({},{})", messageType, consumer);
    consumerMap.put(messageType, consumer);
  }

  /**
   * Deregister.
   *
   * @param messageType the message type
   */
  public void deregister(final String messageType) {
    LOGGER.trace("deregister({})", messageType);
    consumerMap.remove(messageType);
  }

  /**
   * Gets consumer.
   *
   * @param messageType the message type
   * @return the consumer
   */
  public Optional<MessageConsumer> getConsumer(final String messageType) {
    LOGGER.trace("getConsumer({})", messageType);
    return Optional.ofNullable(consumerMap.get(messageType));
  }

  /**
   * Deregister all.
   */
  public void deregisterAll() {
    LOGGER.trace("deregisterAll()");
    consumerMap.clear();
  }

  /**
   * Gets registered message types.
   *
   * @return the registered message types
   */
  public List<String> getRegisteredMessageTypes() {
    LOGGER.trace("getRegisteredMessageTypes()");
    return consumerMap.keySet().stream().toList();
  }
}
