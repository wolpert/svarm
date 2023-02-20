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

package org.svarm.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.node.component.DaggerNodeDropWizardComponent;
import org.svarm.server.Server;
import org.svarm.server.component.DropWizardComponent;
import org.svarm.server.module.DropWizardModule;

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

  @Override
  protected DropWizardComponent dropWizardComponent(final DropWizardModule module) {
    LOGGER.info("dropWizardComponent({})", module);
    return DaggerNodeDropWizardComponent.builder()
        .dropWizardModule(module)
        .build();
  }

}
