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

package com.codeheadsystems.dstore.endtoend;

import static org.slf4j.LoggerFactory.getLogger;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;

/**
 * This class sets up the whole environment making things available for the various services.
 */
public class EnvironmentManager implements TestExecutionListener {
  private static final Logger LOGGER = getLogger(EnvironmentManager.class);

  public static DstoreComponent COMPONENT;

  /**
   * Constructor.
   */
  public EnvironmentManager() {
    LOGGER.info("EnvironmentManager()");
  }


  @Override
  public void testPlanExecutionStarted(final TestPlan testPlan) {
    TestExecutionListener.super.testPlanExecutionStarted(testPlan);
    LOGGER.info("testPlanExecutionStarted({})", testPlan);
    COMPONENT = DaggerDstoreComponent.create();
  }

  @Override
  public void testPlanExecutionFinished(final TestPlan testPlan) {
    TestExecutionListener.super.testPlanExecutionFinished(testPlan);
    LOGGER.info("testPlanExecutionFinished({})", testPlan);
  }
}