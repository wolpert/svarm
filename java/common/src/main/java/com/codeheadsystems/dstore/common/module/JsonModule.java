package com.codeheadsystems.dstore.common.module;

import com.codeheadsystems.dstore.common.factory.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Provides modules needed for JSON support.
 */
@Module
public class JsonModule {

  /**
   * Setups the object mapper for the dagger instance.
   *
   * @param factory that will generate the object mapper.
   * @return the object mapper.
   */
  @Provides
  @Singleton
  public ObjectMapper objectMapper(final ObjectMapperFactory factory) {
    return factory.generate();
  }

}
