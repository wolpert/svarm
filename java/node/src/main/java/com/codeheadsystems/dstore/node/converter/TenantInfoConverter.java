package com.codeheadsystems.dstore.node.converter;

import com.codeheadsystems.dstore.node.api.ImmutableTenantInfo;
import com.codeheadsystems.dstore.node.api.TenantInfo;
import com.codeheadsystems.dstore.node.model.Tenant;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter for tenant info objects.
 */
@Singleton
public class TenantInfoConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenantInfoConverter.class);

  /**
   * Default constructor.
   */
  @Inject
  public TenantInfoConverter() {
    LOGGER.info("TenantInfoConverter()");
  }

  /**
   * Creates a tenant info from a tenant.
   *
   * @param tenant to read from.
   * @return result.
   */
  public TenantInfo from(final Tenant tenant) {
    return ImmutableTenantInfo.builder().id(tenant.id()).build();
  }

}
