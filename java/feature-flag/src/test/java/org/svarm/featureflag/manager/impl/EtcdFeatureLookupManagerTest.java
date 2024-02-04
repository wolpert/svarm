package org.svarm.featureflag.manager.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.svarm.featureflag.manager.impl.EtcdFeatureLookupManager.NAMESPACE;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.accessor.EtcdAccessor;

@ExtendWith(MockitoExtension.class)
class EtcdFeatureLookupManagerTest {

  private static final String FEATURE_ID = "featureId";

  @Mock private EtcdAccessor accessor;

  @InjectMocks private EtcdFeatureLookupManager etcdFeatureLookupManager;

  @Test
  void lookupPercentage_found() {
    when(accessor.get(NAMESPACE, FEATURE_ID)).thenReturn(Optional.of("0.5"));
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(FEATURE_ID);
    assertThat(result).isNotNull()
        .isNotEmpty()
        .contains(0.5);
  }

  @Test
  void setPercentage() {
    etcdFeatureLookupManager.setPercentage(FEATURE_ID, 0.5);
    verify(accessor).put(NAMESPACE, FEATURE_ID, "0.5");
  }

  @Test
  void deletePercentage() {
    etcdFeatureLookupManager.deletePercentage(FEATURE_ID);
    verify(accessor).delete(NAMESPACE, FEATURE_ID);
  }
}