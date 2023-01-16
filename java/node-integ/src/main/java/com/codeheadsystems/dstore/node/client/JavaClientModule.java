package com.codeheadsystems.dstore.node.client;

import com.codeheadsystems.dstore.common.factory.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import feign.micrometer.MicrometerCapability;
import feign.slf4j.Slf4jLogger;
import javax.inject.Singleton;

/**
 * Module for the java client.
 */
@Module
public class JavaClientModule {

  /**
   * Returns a default builder.
   *
   * @param objectMapper needed for encoding/decoding.
   * @return a feign builder.
   */
  @Provides
  @Singleton
  public Feign.Builder builder(final ObjectMapper objectMapper) {
    return Feign.builder()
        .logger(new Slf4jLogger())
        .contract(new JAXRSContract())
        .addCapability(new MicrometerCapability())
        .decoder(new JacksonDecoder(objectMapper))
        .encoder(new JacksonEncoder(objectMapper));
  }

}
