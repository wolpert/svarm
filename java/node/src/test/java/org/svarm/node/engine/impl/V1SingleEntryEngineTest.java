package org.svarm.node.engine.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.common.engine.JsonEngine;
import org.svarm.common.factory.ObjectMapperFactory;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.BaseSQLTest;
import org.svarm.node.api.EntryInfo;
import org.svarm.node.api.ImmutableEntryInfo;
import org.svarm.node.model.ImmutableTenantTable;
import org.svarm.node.model.ImmutableTenantTableIdentifier;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;

class V1SingleEntryEngineTest extends BaseSQLTest {

  private static final String TENANT = "TENANT";
  private static final String TABLE = "TABLE";
  private static final TenantTableIdentifier TENANT_TABLE_IDENTIFIER = ImmutableTenantTableIdentifier.builder()
      .tenantId(TENANT).tableName(TABLE).build();
  private static final TenantTable TENANT_TABLE = ImmutableTenantTable.builder()
      .identifier(TENANT_TABLE_IDENTIFIER).tableVersion(TableDefinition.V1SingleEntryEngine.name()).enabled(true).estimatedQuantity(1)
      .key("KEY").nonce("NONCE").build();
  private JsonEngine jsonEngine;
  private V1SingleEntryEngine engine;

  @BeforeEach
  void setup() {
    jsonEngine = new JsonEngine(new ObjectMapperFactory().generate());
    engine = new V1SingleEntryEngine(metrics, tenantTableDataSourceManager, jsonEngine);
  }

  @Test
  void readWrite() {
    final ObjectNode objectNode = jsonEngine.createObjectNode().put("something", "else");
    final EntryInfo info = ImmutableEntryInfo.builder().id("ID").locationHash(2).timestamp(System.currentTimeMillis())
        .data(objectNode).build();
    engine.write(TENANT_TABLE, info);
    assertThat(engine.read(TENANT_TABLE, info.id()))
        .isNotEmpty()
        .contains(info);
  }

}