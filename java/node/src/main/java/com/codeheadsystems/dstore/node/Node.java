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

package com.codeheadsystems.dstore.node;

import com.codeheadsystems.dstore.common.config.module.EtcdModule;
import com.codeheadsystems.dstore.control.javaclient.module.ControlServiceModule;
import com.codeheadsystems.dstore.node.component.DaggerNodeDropWizardComponent;
import com.codeheadsystems.dstore.node.module.ConfigurationModule;
import com.codeheadsystems.server.Server;
import com.codeheadsystems.server.component.DropWizardComponent;
import com.codeheadsystems.server.module.PreBuiltModule;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is our application itself. Very little here is node specific.
 */
public class Node extends Server<NodeConfiguration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);

  /**
   * Default constructor.
   */
  public Node() {
    LOGGER.info("Node()");
  }


  /**
   * Run the world.
   *
   * @param args from the command line.
   * @throws Exception if we could not start the server.
   */
  public static void main(String[] args) throws Exception {
    LOGGER.info("main({})", (Object) args);
    final Node server = new Node();
    server.run(args);
  }

  /**
   * Creates the component for the dropwizard server.
   *
   * @param configuration our configuration.
   * @param environment   the environment.
   * @param module        the provider for the prebuilt module.
   * @return a build component.
   */
  @Override
  protected DropWizardComponent dropWizardComponent(final NodeConfiguration configuration,
                                                    final Environment environment,
                                                    final PreBuiltModule module) {
    LOGGER.info("dropWizardComponent({})", configuration);
    return DaggerNodeDropWizardComponent.builder()
        .etcdModule(new EtcdModule(configuration.getEtcdConfiguration()))
        .configurationModule(new ConfigurationModule(configuration))
        .controlServiceModule(new ControlServiceModule(configuration.getControlPlaneUrl()))
        .preBuiltModule(module)
        .build();
  }

}
