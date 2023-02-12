/*
 * Copyright (c) 2023. Ned Wolpert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codeheadsystems.dstore.common.config.engine;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.common.config.accessor.EtcdAccessor;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchResponse;
import java.util.Optional;
import java.util.function.Consumer;
import javax.inject.Singleton;
import org.immutables.value.Value;
import org.slf4j.Logger;

/**
 * A watch engine so we are notified of what's going on.
 */
public class WatchEngine {

  private static final Logger LOGGER = getLogger(WatchEngine.class);
  private static final String NAMESPACE = "NAMESPACE";
  private static final String KEY = "KEY";

  private final String namespace;
  private final String key;
  private final Consumer<Event> eventConsumer;
  private final Watch.Watcher watcher;
  private final String tag;

  /**
   * Constructor.
   *
   * @param accessor      for talking to etcd.
   * @param namespace     the namespace.
   * @param key           the key.
   * @param eventConsumer who will get the events.
   */
  @AssistedInject
  public WatchEngine(final EtcdAccessor accessor,
                     @Assisted(NAMESPACE) final String namespace,
                     @Assisted(KEY) final String key,
                     @Assisted final Consumer<Event> eventConsumer) {
    this.tag = namespace + "/" + key;
    this.namespace = namespace;
    this.key = key;
    this.eventConsumer = eventConsumer;
    LOGGER.info("WatchEngine({},{},{})", namespace, key, eventConsumer);
    watcher = accessor.watch(
        namespace,
        key,
        Watch.listener(this::watchResponse, this::error, this::complete));
  }

  /**
   * Closes the watcher and releases all resources.
   */
  public void close() {
    LOGGER.trace("{}:close()", tag);
    watcher.close();
    // TODO: release all resources.
  }

  private void watchResponse(final WatchResponse watchResponse) {
    LOGGER.trace("{}:watchResponse({})", tag, watchResponse);
  }

  private void error(final Throwable throwable) {
    LOGGER.error("{}:error({})", tag, throwable.getMessage(), throwable);
  }

  private void complete() {
    LOGGER.trace("{}:complete()", tag);
  }

  /**
   * Dagger injected assisting factory.
   */
  @AssistedFactory
  @Singleton
  public interface WatchEngineFactory {

    /**
     * Factory to generate a watch engine.
     *
     * @param namespace     to watch.
     * @param key           to watch.
     * @param eventConsumer to consume the events.
     * @return the engine.
     */
    WatchEngine watchEngine(@Assisted(NAMESPACE) final String namespace,
                            @Assisted(KEY) final String key,
                            @Assisted final Consumer<Event> eventConsumer);

  }

  /**
   * Event that happened in the config engine.
   */
  @Value.Immutable
  public interface Event {

    /**
     * Key of the event.
     *
     * @return the key.
     */
    String key();

    /**
     * Value of event. Empty if its a delete.
     *
     * @return the value.
     */
    Optional<String> value();

    /**
     * Type of event.
     *
     * @return the type.
     */
    Type type();

    /**
     * Type of events.
     */
    enum Type {
      /**
       * This is what was putted.
       */
      PUT,
      /**
       * This is what was deleted.
       */
      DELETE
    }
  }

}
