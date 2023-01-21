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

package com.codeheadsystems.dstore.control.resource;

import com.codeheadsystems.dstore.control.dao.KeyDao;
import com.codeheadsystems.dstore.control.model.Key;
import com.codeheadsystems.server.resource.JerseyResource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Default ping resource.
 */
@Singleton
@Path("/")
public class PingResource implements JerseyResource {

  private final KeyDao keyDao;

  /**
   * Constructor.
   *
   * @param keyDao the dao.
   */
  @Inject
  public PingResource(final KeyDao keyDao) {
    this.keyDao = keyDao;
  }

  /**
   * Do something....
   *
   * @param id to find.
   * @return the key.
   */
  @Path("/key/{id}")
  public Key key(@PathParam("id") final String id) {
    return keyDao.read(id);
  }
}
