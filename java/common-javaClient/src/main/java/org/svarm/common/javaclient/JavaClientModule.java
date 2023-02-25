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

package org.svarm.common.javaclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import feign.jaxrs.JakartaContract;
import feign.micrometer.MicrometerCapability;
import feign.slf4j.Slf4jLogger;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Optional;
import javax.inject.Singleton;
import org.svarm.common.javaclient.interceptor.TraceInterceptor;

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
        .contract(new JakartaContract())
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
