package com.codeheadsystems.dstore.node.model;

import org.immutables.value.Value;

/**
 * Internal structure to define the tenant itself.
 */
@Value.Immutable
public interface Tenant {

  /**
   * Id of the tenant. This is global.
   *
   * @return value.
   */
  String id();

  /**
   * Uuid of the tenant. This is local to us.
   *
   * @return value.
   */
  String uuid();

  /**
   * Key of the tenant. This is our local key, not the key from the control plane.
   *
   * @return value.
   */
  String key();

  /**
   * Nonce of the tenant database. This is our local key.
   *
   * @return value.
   */
  String nonce();

}
