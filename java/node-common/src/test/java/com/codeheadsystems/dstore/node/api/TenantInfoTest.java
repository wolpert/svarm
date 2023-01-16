package com.codeheadsystems.dstore.node.api;

import com.codeheadsystems.test.model.BaseJacksonTest;

class TenantInfoTest extends BaseJacksonTest<TenantInfo> {

  @Override
  protected Class<TenantInfo> getBaseClass() {
    return TenantInfo.class;
  }

  @Override
  protected TenantInfo getInstance() {
    return ImmutableTenantInfo.builder().id("id").build();
  }
}