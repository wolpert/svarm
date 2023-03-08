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

package org.svarm.node.module;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.engine.TableDefinitionEngine;
import org.svarm.node.engine.impl.v1singleentry.V1SingleEntryEngine;

/**
 * Binder for table definition engines.
 */
@Module
public interface TableDefinitionEngineModule {

  /**
   * First engine that handles rows with single-entry keys.
   *
   * @param engine instance that was built.
   * @return as a TableDefinitionEngine.
   */
  @Binds
  @IntoMap
  @TableDefinition.TableDefinitionKey(TableDefinition.V1SingleEntryEngine)
  TableDefinitionEngine v1SingleEntry(V1SingleEntryEngine engine);

}
