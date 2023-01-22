package com.codeheadsystems.common.javaclient;

import com.codeheadsystems.common.javaclient.interceptor.TraceInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import feign.micrometer.MicrometerCapability;
import feign.slf4j.Slf4jLogger;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Optional;
import javax.inject.Singleton;

/**
 * Module for the java client.
 */
@Module(includes = {JavaClientModule.Binder.class})
public class JavaClientModule {

  /**
   * Returns a default builder.
   *
   * @param objectMapper     needed for encoding/decoding.
   * @param meterRegistry    for the meter.
   * @param traceInterceptor for dealing with traces.
   * @return a feign builder.
   */
  @Provides
  @Singleton
  public Feign.Builder builder(final ObjectMapper objectMapper,
                               final Optional<MeterRegistry> meterRegistry,
                               final TraceInterceptor traceInterceptor) {
    final MicrometerCapability micrometerCapability = meterRegistry
        .map(MicrometerCapability::new)
        .orElse(new MicrometerCapability());
    return Feign.builder()
        .requestInterceptor(traceInterceptor)
        .logger(new Slf4jLogger())
        .contract(new JAXRSContract())
        .addCapability(micrometerCapability)
        .decoder(new JacksonDecoder(objectMapper))
        .encoder(new JacksonEncoder(objectMapper));
  }

  @Module
  interface Binder {

    @BindsOptionalOf
    MeterRegistry meterRegistry();

  }

}
