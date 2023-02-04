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

package com.codeheadsystems.dstore.endtoend.environment;


import com.codeheadsystems.dstore.endtoend.EnvironmentConfiguration;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PgsqlServiceManager implements ServiceManager {
  private PostgreSQLContainer container;

  @Override
  public void startup(EnvironmentConfiguration configuration) {
    int containerPort = 5432;
    int localPort = 15432;
    DockerImageName postgres = DockerImageName.parse("postgres:14.6");
    container = new PostgreSQLContainer<>(postgres)
        .withDatabaseName("endToEnd")
        .withUsername("control")
        .withPassword("control")
        .withReuse(true)
        .withExposedPorts(containerPort)
        .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
            new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(localPort), new ExposedPort(containerPort)))
        ));
    container.start();
  }

  @Override
  public void shutdown(EnvironmentConfiguration configuration) {
    container.stop();
  }
}
