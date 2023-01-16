package com.codeheadsystems.dstore.node.javaclient;

import com.codeheadsystems.dstore.common.module.JsonModule;
import com.codeheadsystems.dstore.node.api.NodeService;
import com.codeheadsystems.dstore.node.javaclient.module.JavaClientModule;
import com.codeheadsystems.dstore.node.javaclient.module.NodeServiceModule;
import dagger.Component;
import javax.inject.Singleton;

/**
 * Creates a NodeService java client.
 */
@Component(modules = {
    JavaClientModule.class,
    JsonModule.class,
    NodeServiceModule.class
})
@Singleton
public interface NodeServiceComponent {

  /**
   * Usable to create a fresh java client.
   *
   * @param connectionUrl to connect to.
   * @return a node service impl.
   */
  static NodeService generate(final String connectionUrl) {
    return DaggerNodeServiceComponent.builder()
        .nodeServiceModule(new NodeServiceModule(connectionUrl))
        .build().javaClient();
  }

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  NodeService javaClient();

}
