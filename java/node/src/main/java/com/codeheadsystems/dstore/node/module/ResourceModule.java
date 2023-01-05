package com.codeheadsystems.dstore.node.module;

import com.codeheadsystems.dstore.node.resource.JerseyResource;
import com.codeheadsystems.dstore.node.resource.PingResource;
import com.codeheadsystems.dstore.node.resource.TenantResource;
import com.codeheadsystems.dstore.node.resource.TraceUUID;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

/**
 * List of resource implementations we support.
 */
@Module
public interface ResourceModule {

  /**
   * Fake resource.
   *
   * @param resource resource
   * @return JerseyResource.
   */
  @Binds
  @IntoSet
  JerseyResource pingResource(PingResource resource);

  /**
   * Tenant resource.
   *
   * @param resource resource
   * @return JerseyResource.
   */
  @Binds
  @IntoSet
  JerseyResource tenantResource(TenantResource resource);

  /**
   * TraceUUID resource.
   *
   * @param resource resource.
   * @return JerseyResource.
   */
  @Binds
  @IntoSet
  JerseyResource traceUUID(TraceUUID resource);
}
