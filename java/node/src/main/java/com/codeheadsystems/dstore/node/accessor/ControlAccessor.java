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

package com.codeheadsystems.dstore.node.accessor;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Provides access to the control plane.
 */
@Singleton
public class ControlAccessor {

  private static final Logger LOGGER = getLogger(ControlAccessor.class);

  private final ControlNodeService controlNodeService;

  /**
   * Constructor.
   *
   * @param controlNodeService for accessing the service.
   */
  @Inject
  public ControlAccessor(final ControlNodeService controlNodeService) {
    this.controlNodeService = controlNodeService;
    LOGGER.info("ControlAccessor()");
  }


}
