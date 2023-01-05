package com.codeheadsystems.dstore.node.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResponseObjectTest {

  @Test
  void testUuidUnique() {
    final ResponseObject<String> r1 = ImmutableResponseObject.<String>builder().resource("something").build();
    final ResponseObject<String> r2 = ImmutableResponseObject.<String>builder().resource("something").build();
    assertThat(r1.responseUuid()).isNotEqualTo(r2.responseUuid());
  }

}