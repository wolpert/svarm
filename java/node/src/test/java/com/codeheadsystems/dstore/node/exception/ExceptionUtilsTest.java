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

package com.codeheadsystems.dstore.node.exception;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExceptionUtilsTest {

  private static final Object FOUND = new Object();

  @Mock private ExceptionUtils.SupplierWithException<Object> supplier;

  @InjectMocks private ExceptionUtils exceptionUtils;

  @Test
  public void testGet_HappyPath() throws ExecutionException {
    when(supplier.get()).thenReturn(FOUND);
    assertThat(exceptionUtils.loadingCacheExecutionExceptionWrapper(supplier))
        .isPresent()
        .contains(FOUND);
  }

  @Test
  public void testGet_NotFoundException() throws ExecutionException {
    when(supplier.get()).thenThrow(new NotFoundException());
    assertThat(exceptionUtils.loadingCacheExecutionExceptionWrapper(supplier))
        .isNotPresent();
  }

  @Test
  public void testGet_ExecutionException_NotFound() throws ExecutionException {
    when(supplier.get()).thenThrow(new ExecutionException(new NotFoundException()));
    assertThat(exceptionUtils.loadingCacheExecutionExceptionWrapper(supplier))
        .isNotPresent();
  }

  @Test
  public void testGet_ExecutionException_IllegalArgumentException() throws ExecutionException {
    when(supplier.get()).thenThrow(new ExecutionException(new IllegalArgumentException()));
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> exceptionUtils.loadingCacheExecutionExceptionWrapper(supplier));
  }

  @Test
  public void testGet_UncheckedExecutionException_NotFound() throws ExecutionException {
    when(supplier.get()).thenThrow(new UncheckedExecutionException(new NotFoundException()));
    assertThat(exceptionUtils.loadingCacheExecutionExceptionWrapper(supplier))
        .isNotPresent();
  }

  @Test
  public void testGet_UncheckedExecutionException_IllegalArgumentException() throws ExecutionException {
    when(supplier.get()).thenThrow(new UncheckedExecutionException(new IllegalArgumentException()));
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> exceptionUtils.loadingCacheExecutionExceptionWrapper(supplier));
  }

}