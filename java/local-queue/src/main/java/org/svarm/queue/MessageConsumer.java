package org.svarm.queue;

import java.util.function.Consumer;

/**
 * The interface Message consumer. Simplifies dagger injection.
 */
@FunctionalInterface
public interface MessageConsumer extends Consumer<Message> {
}
