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

package com.codeheadsystems.dstore.control.manager;

import com.codeheadsystems.dstore.control.common.api.NodeMetaData;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides verification services for nodes.
 * TODO: do something real man.
 */
@Singleton
public class NodeVerificationManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeVerificationManager.class);

  /**
   * Constructor.
   */
  @Inject
  public NodeVerificationManager() {
    LOGGER.info("NodeVerificationManager()");
    LOGGER.warn("\n---\n--- Warning: Allow All Node Verification Manager Installed\n---");
  }

  /**
   * Verifies the node.
   *
   * @param uuid     to verify.
   * @param metaData their meta data.
   * @return the verification status.
   */
  public boolean verify(final String uuid, final NodeMetaData metaData) {
    LOGGER.warn("verify({},{}) : ALWAYS TRUE", uuid, metaData);
    return true;
  }

}
