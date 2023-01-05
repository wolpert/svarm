package com.codeheadsystems.dstore.node.resource;

import com.codahale.metrics.annotation.Timed;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.codeheadsystems.metrics.Metrics;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Ping.
 */
@Singleton
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public class PingResource implements JerseyResource {

  private final Metrics metrics;

  /**
   * Default constructor.
   *
   * @param configuration         to use.
   * @param internalConfiguration to use.
   * @param metrics               to use.
   */
  @Inject
  public PingResource(final NodeConfiguration configuration,
                      final NodeInternalConfiguration internalConfiguration,
                      final Metrics metrics) {
    this.metrics = metrics;
  }

  /**
   * Ping!
   *
   * @return pong.
   */
  @GET
  @Timed
  @Path("/ping")
  public String ping() {
    return "Pong";
  }

}
