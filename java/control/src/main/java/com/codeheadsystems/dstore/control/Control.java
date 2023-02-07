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

package com.codeheadsystems.dstore.control;

import com.codeheadsystems.dstore.common.config.module.EtcdModule;
import com.codeheadsystems.dstore.control.component.DaggerControlDropWizardComponent;
import com.codeheadsystems.dstore.control.module.ConfigurationModule;
import com.codeheadsystems.server.Server;
import com.codeheadsystems.server.component.DropWizardComponent;
import com.codeheadsystems.server.module.DropWizardModule;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The control plane server.
 */
public class Control extends Server<ControlConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(Control.class);

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
  protected DropWizardComponent dropWizardComponent(final ControlConfiguration configuration,
                                                    final Environment environment,
                                                    final DropWizardModule module) {
    return DaggerControlDropWizardComponent.builder()
        .configurationModule(new ConfigurationModule(configuration, environment))
        .dropWizardModule(module)
        .build();
  }
}
