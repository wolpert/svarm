package com.codeheadsystems.dstore.node.module;

import com.codeheadsystems.dstore.node.manager.DataSourceManager;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.dropwizard.lifecycle.Managed;

/**
 * List your services here that need startup support.
 */
@Module
public interface StartupModule {

  /**
   * Methods that need startup before execution.
   *
   * @param manager to start.
   * @return startup method.
   */
  @Binds
  @IntoSet
  Managed dataSourceManager(final DataSourceManager manager);

}
