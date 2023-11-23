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

package org.svarm.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.control.component.DaggerControlDropWizardComponent;
import org.svarm.server.Server;
import org.svarm.server.component.DropWizardComponent;
import org.svarm.server.module.DropWizardModule;

/**
 * The control plane server.
 */
public class Control extends Server<ControlConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(Control.class);

  /**
   * Instantiates a new Control.
   */
  public Control() {
    super();
  }

  /**
   * Run the world.
   *
   * @param args from the command line.
   * @throws Exception if we could not start the server.
   */
  public static void main(String[] args) throws Exception {
    LOGGER.info("main({})", (Object) args);
    final Control server = new Control();
    server.run(args);
  }

  @Override
  protected DropWizardComponent dropWizardComponent(final DropWizardModule module) {
    return DaggerControlDropWizardComponent.builder()
        .dropWizardModule(module)
        .build();
  }
}
