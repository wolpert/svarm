package org.svarm.common.javaclient;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JakartaContract;
import feign.micrometer.MicrometerCapability;
import feign.slf4j.Slf4jLogger;
import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.javaclient.interceptor.TraceInterceptor;

/**
 * Instruments the feign builder provided.
 */
@Singleton
public class FeignBuilderInstrumentator {

  private static final Logger LOGGER = getLogger(FeignBuilderInstrumentator.class);

  private final ObjectMapper objectMapper;
  private final MeterRegistry meterRegistry;
  private final TraceInterceptor traceInterceptor;

  /**
   * Constructor.
   *
   * @param meterRegistry    for metrics.
   * @param objectMapper     for the mapper.
   * @param traceInterceptor for tracing.
   */
  @Inject
  public FeignBuilderInstrumentator(final ObjectMapper objectMapper,
                                    final MeterRegistry meterRegistry,
                                    final TraceInterceptor traceInterceptor) {
    this.objectMapper = objectMapper;
    this.meterRegistry = meterRegistry;
    this.traceInterceptor = traceInterceptor;
    LOGGER.info("FeignBuilderInstrumentator({},{},{})", objectMapper, meterRegistry, traceInterceptor);
  }

  /**
   * Instruments the builder.
   *
   * @param builder to instrument.
   * @return the builder.
   */
  public Feign.Builder instrument(final Feign.Builder builder) {
    LOGGER.trace("instrument({})", builder);
    final MicrometerCapability micrometerCapability = new MicrometerCapability(meterRegistry);
    return Feign.builder()
        .requestInterceptor(traceInterceptor)
        .logger(new Slf4jLogger())
        .contract(new JakartaContract())
        .addCapability(micrometerCapability)
        .decoder(new JacksonDecoder(objectMapper))
        .encoder(new JacksonEncoder(objectMapper));
  }

}
