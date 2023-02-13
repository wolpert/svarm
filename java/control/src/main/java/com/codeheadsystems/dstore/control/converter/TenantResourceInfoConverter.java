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

import com.codeheadsystems.dstore.control.common.api.ImmutableTenantResourceInfo;
import com.codeheadsystems.dstore.control.common.api.TenantResourceInfo;
import com.codeheadsystems.dstore.control.model.NodeRange;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Converts values for tenant table infos.
 */
@Singleton
public class TenantResourceInfoConverter {
  private static final Logger LOGGER = getLogger(TenantResourceInfoConverter.class);


  /**
   * Constructor.
   */
  @Inject
  public TenantResourceInfoConverter() {
    LOGGER.info("TenantResourceInfoConverter()");
  }

  /**
   * Convert from.
   *
   * @param nodeRanges to convert from.
   * @return to a tenant table info.
   */
  public TenantResourceInfo from(final List<NodeRange> nodeRanges) {
    LOGGER.trace("from({})", nodeRanges);
    final NodeRange nodeRange = nodeRanges.get(0);
    return ImmutableTenantResourceInfo.builder()
        .tenantId(nodeRange.tenant())
        .resource(nodeRange.resource())
        .ready(nodeRanges.stream().allMatch(NodeRange::ready))
        .build();
  }

}
