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

package com.codeheadsystems.dstore.node.module;

import static com.codeheadsystems.dstore.node.engine.impl.V1SingleEntryEngine.DEFINITION_NAME;

import com.codeheadsystems.dstore.node.engine.TableDefinitionEngine;
import com.codeheadsystems.dstore.node.engine.impl.V1SingleEntryEngine;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

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
  @StringKey(DEFINITION_NAME)
  TableDefinitionEngine v1SingleEntry(V1SingleEntryEngine engine);

}
