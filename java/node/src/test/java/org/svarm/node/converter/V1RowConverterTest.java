package org.svarm.node.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.common.engine.JsonEngine;
import org.svarm.common.factory.ObjectMapperFactory;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.api.ImmutableEntryInfo;
import org.svarm.node.model.V1Row;

class V1RowConverterTest {

  private ObjectMapper objectMapper;
  private JsonEngine jsonEngine;
  private V1RowConverter converter;

  @BeforeEach
  void setup() {
    objectMapper = new ObjectMapperFactory().generate();
    jsonEngine = new JsonEngine(objectMapper);
    converter = new V1RowConverter(jsonEngine);
  }

  @Test
  void roundTrip() throws JsonProcessingException {
    final String json = """
        {
          "a":1,"b":2,"c":"d"
        }
        """;
    final JsonNode node = objectMapper.readValue(json, JsonNode.class);
    final EntryInfo info = ImmutableEntryInfo.builder()
        .id("an id").timestamp(554323l).locationHash(3245).data(node).build();
    final List<V1Row> rows = converter.toV1Rows(info);
    final EntryInfo result = converter.toEntryInfo(rows);
    Assertions.assertThat(result).isEqualTo(info);
  }

}