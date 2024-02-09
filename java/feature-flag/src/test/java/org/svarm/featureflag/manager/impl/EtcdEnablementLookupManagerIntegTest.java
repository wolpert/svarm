package org.svarm.featureflag.manager.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.metrics.test.BaseMetricTest;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.test.EtcdClusterExtension;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.svarm.common.config.accessor.EtcdAccessor;

@Tag("integ")
public class EtcdEnablementLookupManagerIntegTest extends BaseMetricTest {
  @RegisterExtension
  public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
      .withNodes(1)
      .build();
  private Client client;
  private EtcdAccessor accessor;
  private EtcdFeatureLookupManager etcdFeatureLookupManager;
  private String featureId;


  @BeforeEach
  void setupClient() {
    client = Client.builder().endpoints(cluster.clientEndpoints()).build();
    accessor = new EtcdAccessor(client, "test", metrics);
    etcdFeatureLookupManager = new EtcdFeatureLookupManager(accessor);
    featureId = UUID.randomUUID().toString();
  }

  @AfterEach
  void tearDownClient() {
    client.close();
  }


  @Test
  void lookupPercentage_found() {
    etcdFeatureLookupManager.setPercentage(featureId, 0.5);
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(featureId);
    assertThat(result).isNotNull()
        .isNotEmpty()
        .contains(0.5);
  }

  @Test
  void lookupPercentage_notFound() {
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(featureId);
    assertThat(result).isNotNull()
        .isEmpty();
  }


  @Test
  void deletePercentage() {
    etcdFeatureLookupManager.setPercentage(featureId, 0.5);
    assertThat(etcdFeatureLookupManager.lookupPercentage(featureId)).isNotEmpty().contains(0.5);
    etcdFeatureLookupManager.deletePercentage(featureId);
    assertThat(etcdFeatureLookupManager.lookupPercentage(featureId)).isEmpty();
  }
}
