package org.svarm.node.engine.impl.v1singleentry;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.engine.JsonEngine;
import org.svarm.common.factory.ObjectMapperFactory;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.BaseSQLTest;
import org.svarm.node.NodeConfiguration;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.api.ImmutableEntryInfo;
import org.svarm.node.model.ImmutableTenantTable;
import org.svarm.node.model.ImmutableTenantTableIdentifier;
import org.svarm.node.model.ImmutableTombstoneConfiguration;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;
import org.svarm.node.model.TombstoneConfiguration;

@ExtendWith(MockitoExtension.class)
class V1SingleEntryEngineTest extends BaseSQLTest {

  private static final String TENANT = "TENANT";
  private static final String TABLE = "TABLE";
  private static final TenantTableIdentifier TENANT_TABLE_IDENTIFIER = ImmutableTenantTableIdentifier.builder()
      .tenantId(TENANT).tableName(TABLE).build();
  private static final TenantTable TENANT_TABLE = ImmutableTenantTable.builder()
      .identifier(TENANT_TABLE_IDENTIFIER).tableVersion(TableDefinition.V1SingleEntryEngine.name()).enabled(true).estimatedQuantity(1)
      .key("KEY").nonce("NONCE").build();
  private JsonEngine jsonEngine;
  private V1RowConverter converter;
  private V1SingleEntryEngine engine;


  @BeforeEach
  void setup() {
    jsonEngine = new JsonEngine(new ObjectMapperFactory().generate());
    converter = new V1RowConverter(jsonEngine);
    final TombstoneConfiguration tombstoneConfiguration = ImmutableTombstoneConfiguration.builder().expiryDuration(java.time.Duration.ofSeconds(0)).build();
    engine = new V1SingleEntryEngine(metrics, tenantTableJdbiManager, converter, tombstoneConfiguration);
  }

  @Test
  void readWrite() {
    final ObjectNode objectNode = jsonEngine.createObjectNode()
        .put("something", "else")
        .put("number", 5)
        .put("other", "thing");
    final EntryInfo info = ImmutableEntryInfo.builder().id("ID").locationHash(2).timestamp(System.currentTimeMillis())
        .data(objectNode).build();
    engine.write(TENANT_TABLE, info);
    assertThat(engine.read(TENANT_TABLE, info.id()))
        .isNotEmpty()
        .contains(info);
    assertThat(engine.keys(TENANT_TABLE, info.id()))
        .hasSize(3)
        .contains("something", "number", "other");
    final ObjectNode objectNodeUpdated = objectNode.deepCopy();
    objectNodeUpdated.remove("other");
    objectNodeUpdated.put("number", "seven");
    objectNodeUpdated.put("ANewField", "doesIt work?");
    final EntryInfo infoUpdated = ImmutableEntryInfo.copyOf(info).withData(objectNodeUpdated);
    engine.write(TENANT_TABLE, infoUpdated);
    assertThat(engine.read(TENANT_TABLE, info.id()))
        .isNotEmpty()
        .contains(infoUpdated);
    assertThat(engine.keys(TENANT_TABLE, info.id()))
        .hasSize(4)
        .contains("something", "number", "ANewField", "other"); // other is still there, but should be null
    engine.clearTombstones(TENANT_TABLE);
    assertThat(engine.keys(TENANT_TABLE, info.id()))
        .hasSize(3)
        .contains("something", "number", "ANewField"); // other is now gone
    engine.delete(TENANT_TABLE, info.id());
    assertThat(engine.read(TENANT_TABLE, info.id()))
        .isEmpty();
  }

}