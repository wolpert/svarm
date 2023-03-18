/*
 * Copyright (c) 2023. Ned Wolpert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.svarm.node.manager;

import java.nio.ByteBuffer;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svarm.common.crypt.CryptUtils;
import org.svarm.node.model.NodeInternalConfiguration;
import org.svarm.node.model.TenantTableIdentifier;

/**
 * Fakes out the control plane. This whole class should eventually be replaced by a real control plane, or a
 * null control plane.
 */
@Singleton
public class FakeControlPlaneManager implements ControlPlaneManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FakeControlPlaneManager.class);
  private final String key;

  /**
   * Default constructor.
   *
   * @param configuration to use.
   * @param cryptUtils    to use.
   */
  @Inject
  public FakeControlPlaneManager(final NodeInternalConfiguration configuration,
                                 final CryptUtils cryptUtils) {
    LOGGER.warn("\n---\n--- FAKE CONTROL PLANE MANAGER IS IN USE!\n---");
    final UUID uuid = UUID.fromString(configuration.uuid());
    final long least = uuid.getLeastSignificantBits();
    final long most = uuid.getMostSignificantBits();
    final byte[] array = new byte[32];
    ByteBuffer.wrap(array).putLong(least).putLong(most).putLong(least).putLong(most);
    key = cryptUtils.toBase64(array);
    LOGGER.warn("FAKE KEY: {}", key);
  }

  @Override
  public boolean verifyRegistration() {
    return true;
  }

  /**
   * Fake keyForNode.
   *
   * @return key based on node uuid.
   */
  @Override
  public String keyForNode() {
    return key;
  }

  /**
   * Fake tenantIdKey.
   *
   * @param tenantId to use.
   * @return key based on node uuid.
   */
  @Override
  public String keyForTenant(final String tenantId) {
    return key;
  }

  @Override
  public void enable(final TenantTableIdentifier identifier) {
    // Nothing to do.
  }

  @Override
  public void delete(final TenantTableIdentifier identifier) {
    // Nothing to do.
  }
}
