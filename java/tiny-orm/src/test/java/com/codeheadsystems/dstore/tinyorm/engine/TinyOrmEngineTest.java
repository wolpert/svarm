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

package com.codeheadsystems.dstore.tinyorm.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.dstore.tinyorm.annotation.ColName;
import com.codeheadsystems.dstore.tinyorm.factory.ColumnDefinitionFactory;
import java.util.Optional;
import org.immutables.value.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TinyOrmEngineTest {

  private static String TABLE_NAME = "tablename";

  private TinyOrmEngine engine;

  @BeforeEach
  void setupEngine() {
    engine = new TinyOrmEngine(new ColumnDefinitionFactory(), new MethodInvokerEngine());
  }

  @Test
  void insertString() {
    assertThat(engine.insertQuery(TestModel.class, TABLE_NAME))
        .isEqualTo("insert into " + TABLE_NAME + " (INTEGER_VALUE,STRING_VALUE) values (?,?)");
  }


  @Value.Immutable
  public interface TestModel {

    @ColName("STRING_VALUE")
    String stringValue();

    @ColName("INTEGER_VALUE")
    Integer integerValue();

    Optional<String> ignoredValue();

  }

}
