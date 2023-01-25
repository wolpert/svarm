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

package com.codeheadsystems.dstore.control.component;

import com.codeheadsystems.dstore.control.module.ConfigurationModule;
import com.codeheadsystems.dstore.control.module.DatabaseModule;
import com.codeheadsystems.dstore.control.module.ResourceModule;
import com.codeheadsystems.dstore.control.module.UtilitiesModule;
import com.codeheadsystems.server.component.DropWizardComponent;
import com.codeheadsystems.server.module.DropWizardModule;
import com.codeheadsystems.server.module.EtcdModule;
import dagger.Component;
import javax.inject.Singleton;

/**
 * Creates the pieces needed for the control plane to run.
 */
@Component(modules = {
    ConfigurationModule.class,
    DatabaseModule.class,
    DropWizardModule.class,
    EtcdModule.class,
    ResourceModule.class,
    UtilitiesModule.class
})
@Singleton
public interface ControlDropWizardComponent extends DropWizardComponent {
}
