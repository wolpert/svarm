package com.codeheadsystems.dstore.node.api;

import com.codeheadsystems.test.model.BaseJacksonTest;

class TenantTableInfoTest extends BaseJacksonTest<TenantTableInfo> {

  @Override
  protected Class<TenantTableInfo> getBaseClass() {
    return TenantTableInfo.class;
  }

  @Override
  protected TenantTableInfo getInstance() {
    return ImmutableTenantTableInfo.builder().id("ID").build();
  }
}