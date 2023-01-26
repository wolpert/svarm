/*
 * Copyright (c) 2022-2023. Ned Wolpert
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

package com.codeheadsystems.dstore.node.component;

import com.codeheadsystems.dstore.common.config.module.EtcdModule;
import com.codeheadsystems.dstore.node.module.ConfigurationModule;
import com.codeheadsystems.dstore.node.module.DataSourceModule;
import com.codeheadsystems.dstore.node.module.HealthCheckModule;
import com.codeheadsystems.dstore.node.module.ResourceModule;
import com.codeheadsystems.dstore.node.module.TableDefinitionEngineModule;
import com.codeheadsystems.dstore.node.module.UtilitiesModule;
import com.codeheadsystems.server.component.DropWizardComponent;
import com.codeheadsystems.server.module.DropWizardModule;
import dagger.Component;
import javax.inject.Singleton;

/**
 * All the necessary goodness to create the app.
 */
@Singleton
@Component(modules = {
    ConfigurationModule.class,
    DataSourceModule.class,
    DropWizardModule.class,
    EtcdModule.class,
    HealthCheckModule.class,
    ResourceModule.class,
    TableDefinitionEngineModule.class,
    UtilitiesModule.class
})
public interface NodeDropWizardComponent extends DropWizardComponent {

}
