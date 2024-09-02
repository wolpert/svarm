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

package org.svarm.node.component;

import dagger.Component;
import javax.inject.Singleton;
import org.svarm.common.config.module.EtcdModule;
import org.svarm.common.javaclient.JavaClientModule;
import org.svarm.control.javaclient.module.ControlServiceModule;
import org.svarm.node.module.ControlPlaneModule;
import org.svarm.node.module.DataSourceModule;
import org.svarm.node.module.FeatureFlagModule;
import org.svarm.node.module.HealthCheckModule;
import org.svarm.node.module.NodeConfigurationModule;
import org.svarm.node.module.ResourceModule;
import org.svarm.node.module.TableDefinitionEngineModule;
import org.svarm.node.module.UtilitiesModule;
import com.codeheadsystems.queue.module.QueueModule;
import org.svarm.server.component.DropWizardComponent;
import org.svarm.server.module.DropWizardModule;

/**
 * All the necessary goodness to create the app.
 */
@Singleton
@Component(modules = {
    ControlPlaneModule.class,
    ControlServiceModule.class,
    DataSourceModule.class,
    DropWizardModule.class,
    EtcdModule.class,
    FeatureFlagModule.class,
    HealthCheckModule.class,
    NodeConfigurationModule.class,
    JavaClientModule.class,
    ResourceModule.class,
    TableDefinitionEngineModule.class,
    UtilitiesModule.class,
    QueueModule.class
})
public interface NodeDropWizardComponent extends DropWizardComponent {

}
