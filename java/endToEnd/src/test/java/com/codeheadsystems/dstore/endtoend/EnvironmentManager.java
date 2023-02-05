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

import com.codeheadsystems.dstore.endtoend.environment.ControlServiceManager;
import com.codeheadsystems.dstore.endtoend.environment.EtcdServiceManager;
import com.codeheadsystems.dstore.endtoend.environment.NodeServiceManager;
import com.codeheadsystems.dstore.endtoend.environment.PgsqlServiceManager;
import com.codeheadsystems.dstore.endtoend.environment.ServiceManager;
import java.util.HashMap;
import java.util.Map;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;

/**
 * This class sets up the whole environment making things available for the various services.
 */
public class EnvironmentManager implements TestExecutionListener {
  private static final Logger LOGGER = getLogger(EnvironmentManager.class);
  private static final Map<String, Throwable> INITIALIZATION_FAILURE = new HashMap<>();
  private ServiceManager[] managers;
  private EnvironmentConfiguration environmentConfiguration;

  /**
   * Constructor.
   */
  public EnvironmentManager() {
    LOGGER.info("EnvironmentManager()");
  }

  private static void addException(final String service, final Throwable exception) {
    LOGGER.warn("Exception for {}", service, exception);
    synchronized (INITIALIZATION_FAILURE) {
      INITIALIZATION_FAILURE.put(service, exception);
    }
  }

  public static Map<String, Throwable> getInitializationFailure() {
    synchronized (INITIALIZATION_FAILURE) {
      return new HashMap<>(INITIALIZATION_FAILURE);
    }
  }

  @Override
  public void testPlanExecutionStarted(final TestPlan testPlan) {
    environmentConfiguration = new EnvironmentConfiguration();
    TestExecutionListener.super.testPlanExecutionStarted(testPlan);
    LOGGER.info("testPlanExecutionStarted({})", testPlan);
    managers = generateList();
    for (int i = 0; i < managers.length; i++) { // go forward
      final ServiceManager m = managers[i];
      final String name = m.getClass().getSimpleName();
      LOGGER.info("Startup: {}", name);
      try {
        m.startup(environmentConfiguration);
      } catch (Throwable t) {
        addException(name, t);
      }
    }
  }

  private ServiceManager[] generateList() {
    return new ServiceManager[]{
        new EtcdServiceManager(),
        new PgsqlServiceManager(),
        new ControlServiceManager(),
        new NodeServiceManager()
    };
  }

  @Override
  public void testPlanExecutionFinished(final TestPlan testPlan) {
    TestExecutionListener.super.testPlanExecutionFinished(testPlan);
    LOGGER.info("testPlanExecutionFinished({})", testPlan);
    for (int i = managers.length - 1; i >= 0; i--) { // go backwards
      final ServiceManager m = managers[i];
      LOGGER.info("Shutdown: {}", m.getClass().getSimpleName());
      m.shutdown(environmentConfiguration);
    }
  }
}
