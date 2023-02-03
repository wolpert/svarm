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

package com.codeheadsystems.dstore.control.converter;

import static org.slf4j.LoggerFactory.getLogger;

import com.codeheadsystems.dstore.control.model.NodeRange;
import com.codeheadsystems.dstore.node.api.ImmutableTenantTableInfo;
import com.codeheadsystems.dstore.node.api.TenantTableInfo;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Converts values for tenant table infos.
 */
@Singleton
public class TenantTableInfoConverter {
  private static final Logger LOGGER = getLogger(TenantTableInfoConverter.class);


  /**
   * Constructor.
   */
  @Inject
  public TenantTableInfoConverter() {
    LOGGER.info("TenantTableInfoConverter()");
  }

  /**
   * Convert from.
   *
   * @param nodeRanges to convert from.
   * @return to a tenant table info.
   */
  public TenantTableInfo from(final List<NodeRange> nodeRanges) {
    LOGGER.trace("from({})", nodeRanges);
    return ImmutableTenantTableInfo.builder()
        .id(nodeRanges.get(0).resource()) // TODO: this is dumb. Maybe control doesn't just reuse the node-common stuff.
        .build();
  }

}
