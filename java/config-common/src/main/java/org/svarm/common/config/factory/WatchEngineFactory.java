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

package org.svarm.common.config.factory;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import java.util.function.Consumer;
import javax.inject.Singleton;
import org.svarm.common.config.engine.Event;
import org.svarm.common.config.engine.WatchEngine;

/**
 * Dagger injected assisting factory.
 */
@AssistedFactory
@Singleton
public interface WatchEngineFactory {

  /**
   * Qualifier for the namespace.
   */
  String NAMESPACE = "WatchEngineFactory.NAMESPACE";

  /**
   * Qualifier for the key.
   */
  String KEY = "WatchEngineFactory.KEY";

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
