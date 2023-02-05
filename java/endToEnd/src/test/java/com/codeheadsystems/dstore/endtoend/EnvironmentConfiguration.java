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

package com.codeheadsystems.dstore.endtoend;

import io.etcd.jetcd.Client;
import java.util.List;

public class EnvironmentConfiguration {

  private String controlConnectionUrl;
  private String nodeConnectionUrl;
  private List<String> endpoints;
  private Client etcdClient;

  public List<String> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(final List<String> endpoints) {
    this.endpoints = endpoints;
  }


  public String getControlConnectionUrl() {
    return controlConnectionUrl;
  }

  public void setControlConnectionUrl(final String controlConnectionUrl) {
    this.controlConnectionUrl = controlConnectionUrl;
  }

  public String getNodeConnectionUrl() {
    return nodeConnectionUrl;
  }

  public void setNodeConnectionUrl(final String nodeConnectionUrl) {
    this.nodeConnectionUrl = nodeConnectionUrl;
  }

  public Client getEtcdClient() {
    return etcdClient;
  }

  public void setEtcdClient(final Client etcdClient) {
    this.etcdClient = etcdClient;
  }
}