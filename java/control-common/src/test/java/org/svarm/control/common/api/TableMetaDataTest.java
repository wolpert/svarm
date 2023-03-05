package org.svarm.control.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.test.model.BaseJacksonTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.svarm.datastore.common.TableDefinition;

class TableMetaDataTest extends BaseJacksonTest<TableMetaData> {

  @Override
  protected Class<TableMetaData> getBaseClass() {
    return TableMetaData.class;
  }

  @Override
  protected TableMetaData getInstance() {
    return ImmutableTableMetaData.builder().tableDefinition(TableDefinition.V1SingleEntryEngine).build();
  }

  @Test
  void FromParentClass() throws JsonProcessingException {
    final TableMetaData instance = getInstance();
    final String json = objectMapper().writeValueAsString(instance);
    System.out.println(json);
    final ResourceMetaData metaData = objectMapper.readValue(json, ResourceMetaData.class);
    assertThat(metaData)
        .isInstanceOf(TableMetaData.class)
        .isEqualTo(instance);
  }
}