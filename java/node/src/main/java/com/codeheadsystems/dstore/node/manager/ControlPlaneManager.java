package com.codeheadsystems.dstore.node.manager;

/**
 * Provides methods to talk to the control plane.
 */
public interface ControlPlaneManager {

  /**
   * Returns the key base64 encoded for this node.
   *
   * @return the key.
   */
  String keyForNode();

  /**
   * Returns the key base64 encoded for this tenant.
   *
   * @param tenantId to use.
   * @return the key.
   */
  String keyForTenant(String tenantId);

}
