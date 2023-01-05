package com.codeheadsystems.dstore.node.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codeheadsystems.dstore.common.crypt.CryptUtils;
import com.codeheadsystems.dstore.node.NodeConfiguration;
import com.codeheadsystems.dstore.node.manager.ControlPlaneManager;
import com.codeheadsystems.dstore.node.model.NodeInternalConfiguration;
import com.codeheadsystems.dstore.node.util.DeletingFileVisitor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionEngineTest {

  private static final String KEY = "KEY";
  private static final String NODE = "NODE";
  private static final String NONCE = "NONCE";
  private static final String TENANT = "TENANT";
  private static final String TENANT_NONCE = "TENANT_NONCE";
  private static Path TEMP_DIR;
  private Random random = new Random();
  @Mock private NodeConfiguration nodeConfiguration;
  @Mock private CryptUtils cryptUtils;
  @Mock private NodeInternalConfiguration nodeInternalConfiguration;
  @Mock private ControlPlaneManager controlPlaneManager;
  @InjectMocks private DatabaseConnectionEngine manager;

  @BeforeAll
  static void setupTempDir() throws IOException {
    TEMP_DIR = Files.createTempDirectory("DatabaseConnectionManagerTest-");
  }

  @AfterAll
  static void tearDownTempDir() {
    try {
      Files.walkFileTree(TEMP_DIR, new DeletingFileVisitor());
    } catch (IOException re) {
      System.out.println("Log: Unable to delete " + TEMP_DIR + ":" + re.getClass());
    }
  }

  @Test
  void getInternalConnectionUrl() {
    final byte[] key = new byte[2];
    final byte[] nonce = new byte[2];
    Arrays.fill(key, (byte) 1);
    Arrays.fill(nonce, (byte) 2);
    when(nodeConfiguration.getDatabaseDirectory()).thenReturn(TEMP_DIR.toString());
    when(nodeInternalConfiguration.key()).thenReturn(KEY);
    when(controlPlaneManager.keyForNode()).thenReturn(NODE);
    when(cryptUtils.xor(KEY, NODE)).thenReturn(key);
    when(nodeInternalConfiguration.nonce()).thenReturn(NONCE);
    when(cryptUtils.fromBase64(NONCE)).thenReturn(nonce);
    assertThat(manager.getInternalConnectionUrl())
        .isNotNull()
        .isEqualTo("jdbc:hsqldb:file:" + TEMP_DIR.toString() + "/" + DatabaseConnectionEngine.INTERNAL_DB_NAME +
            "/database;crypt_key=0101;crypt_iv=0202;crypt_type=AES/GCM-SIV/NoPadding;crypt_provider=BC;");
  }

  @Test
  void getTenantConnectionUrl() {
    final byte[] key = new byte[2];
    final byte[] nonce = new byte[2];
    Arrays.fill(key, (byte) 1);
    Arrays.fill(nonce, (byte) 2);
    when(nodeConfiguration.getDatabaseDirectory()).thenReturn(TEMP_DIR.toString());
    when(controlPlaneManager.keyForTenant(TENANT)).thenReturn(TENANT_NONCE);
    when(cryptUtils.xor(KEY, TENANT_NONCE)).thenReturn(key);
    when(cryptUtils.fromBase64(NONCE)).thenReturn(nonce);
    assertThat(manager.getTenantConnectionUrl(TENANT, KEY, NONCE))
        .isNotNull()
        .isEqualTo("jdbc:hsqldb:file:" + TEMP_DIR.toString() + "/" + TENANT +
            "/database;crypt_key=0101;crypt_iv=0202;crypt_type=AES/GCM-SIV/NoPadding;crypt_provider=BC;");
  }

  @Test
  void doesItWork() {
    final byte[] key = new byte[32];
    final byte[] nonce = new byte[12];
    random.nextBytes(key);
    random.nextBytes(nonce);
    String connectionUrl = manager.getConnectionUrl(TEMP_DIR.toString(), key, nonce);
    Connection connection = manager.connection(connectionUrl);
    assertThat(connection).isNotNull();
  }

}