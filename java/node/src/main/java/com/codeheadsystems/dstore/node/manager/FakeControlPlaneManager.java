package com.codeheadsystems.dstore.node.manager;

import com.codeheadsystems.dstore.common.crypt.CryptUtils;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import java.nio.ByteBuffer;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * Fake tenantIdKey
   *
   * @param tenantId to use.
   * @return key based on node uuid.
   */
  @Override
  public String keyForTenant(final String tenantId) {
    return key;
  }
}
