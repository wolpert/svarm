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

package org.svarm.common.engine;

import static org.slf4j.LoggerFactory.getLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Used to verify types of data.
 */
@Singleton
public class VerificationEngine {

  private static final Logger LOGGER = getLogger(VerificationEngine.class);

  /**
   * Constructor.
   */
  @Inject
  public VerificationEngine() {
    LOGGER.info("VerificationEngine()");
  }


}
