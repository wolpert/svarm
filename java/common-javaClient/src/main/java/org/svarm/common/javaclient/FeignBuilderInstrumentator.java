package org.svarm.common.javaclient;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JakartaContract;
import feign.micrometer.MicrometerCapability;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import io.micrometer.core.instrument.MeterRegistry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * Instruments the feign builder provided.
 */
@Singleton
public class FeignBuilderInstrumentator {

  private static final Logger LOGGER = getLogger(FeignBuilderInstrumentator.class);

  private final TraceInterceptor traceInterceptor;
  private final Slf4jLogger slf4jLogger;
  private final MicrometerCapability micrometerCapability;
  private final JakartaContract jakartaContract;
  private final JacksonDecoder jacksonDecoder;
  private final JacksonEncoder jacksonEncoder;
  private final OkHttpClient okHttpClient;

  /**
   * Constructor.
   *
   * @param traceInterceptor for tracing.
   * @param meterRegistry    for metrics.
   * @param objectMapper     for json.
   * @param okHttpClient     the ok http client
   */
  @Inject
  public FeignBuilderInstrumentator(final TraceInterceptor traceInterceptor,
                                    final MeterRegistry meterRegistry,
                                    final ObjectMapper objectMapper,
                                    final OkHttpClient okHttpClient) {
    this.okHttpClient = okHttpClient;
    this.slf4jLogger = new Slf4jLogger();
    this.micrometerCapability = new MicrometerCapability(meterRegistry);
    this.jakartaContract = new JakartaContract();
    this.jacksonDecoder = new JacksonDecoder(objectMapper);
    this.jacksonEncoder = new JacksonEncoder(objectMapper);
    this.traceInterceptor = traceInterceptor;
    LOGGER.info("FeignBuilderInstrumentator({},{},{})", traceInterceptor, meterRegistry, objectMapper);
  }

  /**
   * Instruments the builder.
   *
   * @param builder to instrument.
   * @return the builder.
   */
  public Feign.Builder instrument(final Feign.Builder builder) {
    LOGGER.trace("instrument({})", builder);
    return builder
        .client(okHttpClient)
        .requestInterceptor(traceInterceptor)
        .logger(slf4jLogger)
        .contract(jakartaContract)
        .addCapability(micrometerCapability)
        .decoder(jacksonDecoder)
        .encoder(jacksonEncoder);
  }

  /**
   * Generates a builder with the decorator.
   *
   * @param decorators to decorate.
   * @return the builder.
   */
  public Feign.Builder generate(final FeignDecorators decorators) {
    final Feign.Builder builder = Resilience4jFeign.builder(decorators);
    instrument(builder);
    return builder;
  }
}
