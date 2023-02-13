package com.codeheadsystems.dstore.node.javaclient;

import com.codeheadsystems.common.javaclient.JavaClientModule;
import com.codeheadsystems.dstore.common.module.JsonModule;
import com.codeheadsystems.dstore.node.api.NodeTenantService;
import com.codeheadsystems.dstore.node.api.NodeTenantTableEntryService;
import com.codeheadsystems.dstore.node.api.NodeTenantTableService;
import com.codeheadsystems.dstore.node.javaclient.module.NodeServiceModule;
import dagger.Component;
import javax.inject.Named;
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
  static NodeServiceComponent generate(final String connectionUrl) {
    return DaggerNodeServiceComponent.builder()
        .nodeServiceModule(new NodeServiceModule(connectionUrl))
        .build();
  }

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  NodeTenantService nodeTenantService();

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  NodeTenantTableService nodeTenantTableService();

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  NodeTenantTableEntryService nodeTenantTableEntryService();

}
