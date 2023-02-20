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

package org.svarm.control.converter;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.svarm.control.common.api.ImmutableNodeInfo;
import org.svarm.control.common.api.NodeInfo;
import org.svarm.control.model.Node;

/**
 * Converts stuff to node info.
 */
@Singleton
public class NodeInfoConverter {

  /**
   * Default constructor.
   */
  @Inject
  public NodeInfoConverter() {

  }

  /**
   * Generate a node info.
   *
   * @param node from.
   * @return the to.
   */
  public NodeInfo from(final Node node) {
    return ImmutableNodeInfo.builder()
        .status(node.status())
        .uuid(node.uuid())
        .build();
  }
}
