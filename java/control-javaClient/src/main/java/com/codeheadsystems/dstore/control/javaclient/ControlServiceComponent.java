package com.codeheadsystems.dstore.control.javaclient;

import com.codeheadsystems.common.javaclient.JavaClientModule;
import com.codeheadsystems.dstore.common.module.JsonModule;
import com.codeheadsystems.dstore.control.common.api.ControlNodeService;
import com.codeheadsystems.dstore.control.javaclient.module.ControlServiceModule;
import dagger.Component;
import javax.inject.Singleton;

/**
 * Creates a NodeService java client.
 */
@Component(modules = {
    JavaClientModule.class,
    JsonModule.class,
    ControlServiceModule.class
})
@Singleton
public interface ControlServiceComponent {

  /**
   * Usable to create a fresh java client.
   *
   * @param connectionUrl to connect to.
   * @return a node service impl.
   */
  static ControlServiceComponent controlNodeService(final String connectionUrl) {
    return DaggerControlServiceComponent.builder()
        .controlServiceModule(new ControlServiceModule(connectionUrl))
        .build();
  }

  /**
   * The node service dagger will generate.
   *
   * @return a node service.
   */
  ControlNodeService controlNodeService();

}
