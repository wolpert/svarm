package org.svarm.node.engine.impl.v1singleentry;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.engine.JsonEngine;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.api.ImmutableEntryInfo;

/**
 * Converts between rows and json.
 */
@Singleton
public class V1RowConverter {
  private static final String INTEGER_TYPE = "INTEGER";
  private static final String STRING_TYPE = "STRING";

  private static final Logger LOGGER = getLogger(V1RowConverter.class);

  private final JsonEngine jsonEngine;

  /**
   * Constructor.
   *
   * @param jsonEngine for reading/writing json.
   */
  @Inject
  public V1RowConverter(final JsonEngine jsonEngine) {
    this.jsonEngine = jsonEngine;
    LOGGER.info("V1RowConverter({})", jsonEngine);
  }

  /**
   * Converts a list of rows to an entry info.
   *
   * @param list of rows.
   * @return the entry info.
   */
  public EntryInfo toEntryInfo(final List<V1Row> list) {
    LOGGER.trace("toEntryInfo({})", list);
    final ImmutableEntryInfo.Builder builder = ImmutableEntryInfo.builder();
    final ObjectNode node = jsonEngine.createObjectNode();

    list.forEach(row -> {
      builder.id(row.id());
      builder.locationHash(row.hash());
      builder.timestamp(row.timestamp());
      switch (row.cDataType()) {
        case INTEGER_TYPE -> node.put(row.cCol(), Integer.valueOf(row.cData()));
        case STRING_TYPE -> node.put(row.cCol(), row.cData());
        default -> throw new IllegalArgumentException("Unknown type: " + row.cDataType());
      }
    });
    builder.data(node);
    return builder.build();
  }

  /**
   * Converts an entryInfo to a list of rows.
   *
   * @param entryInfo to convert.
   * @return the rows.
   */
  public List<V1Row> toV1Rows(final EntryInfo entryInfo) {
    LOGGER.trace("toV1Rows({})", entryInfo);
    final ImmutableList.Builder<V1Row> builder = ImmutableList.builder();
    final String id = entryInfo.id();
    final Integer locationHash = entryInfo.locationHash();
    final Long timestamp = entryInfo.timestamp();
    entryInfo.data().fieldNames().forEachRemaining(col -> {
      final JsonNode element = entryInfo.data().get(col);
      final String dataType;
      if (element.isNumber()) {
        dataType = INTEGER_TYPE;
      } else if (element.isTextual()) {
        dataType = STRING_TYPE;
      } else {
        throw new IllegalArgumentException("Unknown type: " + element.getNodeType());
      }
      builder.add(ImmutableV1Row.builder()
          .id(id)
          .hash(locationHash)
          .timestamp(timestamp)
          .cCol(col)
          .cDataType(dataType)
          .cData(element.asText())
          .build());
    });
    return builder.build();
  }
}
