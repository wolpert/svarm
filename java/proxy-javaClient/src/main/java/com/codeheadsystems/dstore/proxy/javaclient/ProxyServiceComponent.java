package com.codeheadsystems.dstore.proxy.javaclient;

import com.codeheadsystems.common.javaclient.JavaClientModule;
import com.codeheadsystems.dstore.common.module.JsonModule;
import com.codeheadsystems.dstore.proxy.common.api.ProxyService;
import com.codeheadsystems.dstore.proxy.javaclient.module.ProxyServiceModule;
import dagger.Component;
import javax.inject.Singleton;

/**
 * Creates a NodeService java client.
 */
@Component(modules = {
    JavaClientModule.class,
    JsonModule.class,
    ProxyServiceModule.class
})
@Singleton
public interface ProxyServiceComponent {

  /**
   * Usable to create a fresh java client.
   *
   * @param connectionUrl to connect to.
   * @return a node service impl.
   */
  static ProxyServiceComponent controlNodeService(final String connectionUrl) {
    return DaggerProxyServiceComponent.builder()
        .proxyServiceModule(new ProxyServiceModule(connectionUrl))
        .build();
  }

  /**
   * The proxy service dagger will generate.
   *
   * @return a node service.
   */
  ProxyService proxyService();


}
