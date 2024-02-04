package org.svarm.featureflag.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.featureflag.factory.FeatureFactory;

@ExtendWith(MockitoExtension.class)
class FeatureManagerTest {

  private static final String FEATURE_ID = "featureId";
  private static final String DISCRIMINATOR = "discriminator";

  @Mock private FeatureLookupManager featureLookupManager;
  @Mock private FeatureFactory featureFactory;

  @InjectMocks private FeatureManager featureManager;

  @Test
  void isEnabled() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.of(0.5));
    when(featureFactory.generate(0.5)).thenReturn(discriminator -> true);

    assertThat(featureManager.isEnabled(FEATURE_ID, DISCRIMINATOR)).isTrue();
  }

  @Test
  void isEnabled_noFeature() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.empty());
    when(featureFactory.disabledFeature()).thenReturn(discriminator -> false);

    assertThat(featureManager.isEnabled(FEATURE_ID, DISCRIMINATOR)).isFalse();
  }

  @Test
  void ifEnabledElse_enabled() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.of(0.5));
    when(featureFactory.generate(0.5)).thenReturn(discriminator -> true);

    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("enabled");
  }

  @Test
  void ifEnabledElse_disabled() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.empty());
    when(featureFactory.disabledFeature()).thenReturn(discriminator -> false);

    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("disabled");
  }

  @Test
  void invalidate() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.empty()).thenReturn(Optional.of(0.5));
    when(featureFactory.disabledFeature()).thenReturn(discriminator -> false);
    when(featureFactory.generate(0.5)).thenReturn(discriminator -> true);

    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("disabled");
    featureManager.invalidate(FEATURE_ID);
    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("enabled");

  }
}