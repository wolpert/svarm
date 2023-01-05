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

import com.codeheadsystems.dstore.node.manager.DataSourceManager;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.dropwizard.lifecycle.Managed;

/**
 * List your services here that need startup support.
 */
@Module
public interface StartupModule {

  /**
   * Methods that need startup before execution.
   *
   * @param manager to start.
   * @return startup method.
   */
  @Binds
  @IntoSet
  Managed dataSourceManager(final DataSourceManager manager);

}
