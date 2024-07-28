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

package org.svarm.node.utils;

import com.codeheadsystems.metrics.Tags;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * Static methods to help with tags.
 * It's unclear if this is a good idea or not. We'll see.
 */
public class TagHelper {

  private TagHelper() {

  }


  /**
   * Provides tags from the object.
   *
   * @param object for our tags.
   * @return the tags.
   */
  public static Tags from(final TenantTable object) {
    return from(object.identifier());
  }

  /**
   * Provides tags from the object.
   *
   * @param object for our tags.
   * @return the tags.
   */
  public static Tags from(final TenantTableIdentifier object) {
    return Tags.of(
        "tenant", object.tenantId(),
        "resource", object.tableName()
    );
  }

}
