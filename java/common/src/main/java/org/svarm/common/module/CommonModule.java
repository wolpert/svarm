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

package org.svarm.common.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import java.time.Clock;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.svarm.common.factory.ObjectMapperFactory;

/**
 * Provides modules needed for JSON support.
 */
@Module(includes = CommonModule.Binder.class)
public class CommonModule {

  /**
   * Identifier for the server object mapper.
   */
  public static final String SERVER_OBJECT_MAPPER = "SERVER OBJECT MAPPER";

  /**
   * Setups the object mapper for the dagger instance.
   *
   * @param factory            that will generate the object mapper.
   * @param serverObjectMapper server object mapper.
   * @return the object mapper.
   */
  @Provides
  @Singleton
  public ObjectMapper objectMapper(final ObjectMapperFactory factory,
                                   @Named(SERVER_OBJECT_MAPPER) Optional<ObjectMapper> serverObjectMapper) {
    return serverObjectMapper.orElseGet(factory::generate);
  }

  /**
   * The clock to use.
   *
   * @return clock.
   */
  @Provides
  @Singleton
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Module
  interface Binder {

    /**
     * Declare this if you want to use instead of the factory.
     *
     * @return object mapper from the server.
     */
    @BindsOptionalOf
    @Named(SERVER_OBJECT_MAPPER)
    ObjectMapper serverObjectMapper();

  }

}
