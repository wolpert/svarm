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

package com.codeheadsystems.dstore.node.manager;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.common.config.engine.Event;
import com.codeheadsystems.dstore.common.config.engine.WatchEngine;
import com.codeheadsystems.dstore.common.config.factory.WatchEngineFactory;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.lifecycle.Managed;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * This class manages the control plane watcher. Which is how we get directions of what to do.
 */
@Singleton
public class ControlPlaneWatcherManager implements Managed {

  private static final Logger LOGGER = getLogger(ControlPlaneWatcherManager.class);

  private final String uuid;
  private final WatchEngineFactory watchEngineFactory;
  private WatchEngine engine;

  /**
   * Constructor.
   *
   * @param configuration to get the uuid.
   * @param factory       to generate the engine.
   */
  @Inject
  public ControlPlaneWatcherManager(final NodeInternalConfiguration configuration,
                                    final WatchEngineFactory factory) {
    LOGGER.info("ControlPlaneWatcherManager({})", factory);
    this.watchEngineFactory = factory;
    this.uuid = configuration.uuid();
  }

  @VisibleForTesting
  void handle(final Event event) {
    LOGGER.trace("handle({})", event);
  }

  @Override
  public void start() throws Exception {
    if (engine == null) {
      LOGGER.info("start()");
      engine = watchEngineFactory.watchEngine("node", uuid, this::handle);
    } else {
      LOGGER.warn("start(): already have an engine");
    }
  }

  @Override
  public void stop() throws Exception {
    if (engine != null) {
      LOGGER.info("stop(): closing");
      engine.close();
      engine = null;
    } else {
      LOGGER.warn("stop(): No engine");
    }
  }
}
