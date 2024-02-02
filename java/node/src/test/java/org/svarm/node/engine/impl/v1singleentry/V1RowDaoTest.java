package org.svarm.node.engine.impl.v1singleentry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.BaseSQLTest;
import org.svarm.node.model.ImmutableTenantTable;
import org.svarm.node.model.ImmutableTenantTableIdentifier;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;

class V1RowDaoTest extends BaseSQLTest {
  private static final String TENANT = "TENANT";
  private static final String TABLE = "TABLE";
  private static final TenantTableIdentifier TENANT_TABLE_IDENTIFIER = ImmutableTenantTableIdentifier.builder()
      .tenantId(TENANT).tableName(TABLE).build();
  private static final TenantTable TENANT_TABLE = ImmutableTenantTable.builder()
      .identifier(TENANT_TABLE_IDENTIFIER).tableVersion(TableDefinition.V1SingleEntryEngine.name()).enabled(true).estimatedQuantity(1)
      .key("KEY").nonce("NONCE").build();
  private V1RowDao dao;
  private Random random = new Random();

  @BeforeEach
  void setup() {
    dao = tenantTableJdbiManager.getV1RowDao(TENANT_TABLE);
  }

  @Test
  void roundTrip() {
    final V1Row row = randomRow();
    assertThat(dao.read(row.hash())).isEmpty();
    dao.insert(row);
    assertThat(dao.read(row.hash())).containsExactly(row);
  }

  @Test
  void roundTripWithoutCData() {
    final V1Row row = ImmutableV1Row.builder().from(randomRow()).cData(Optional.empty()).build();
    assertThat(dao.read(row.hash())).isEmpty();
    dao.insert(row);
    final V1Row result = dao.read(row.hash()).get(0);
    assertThat(result).hasNoNullFieldsOrPropertiesExcept("cData").isEqualTo(row);
  }

  private V1Row randomRow() {
    return ImmutableV1Row.builder()
        .id(UUID.randomUUID().toString())
        .cCol(UUID.randomUUID().toString())
        .hash(random.nextInt())
        .timestamp(random.nextLong())
        .cDataType(UUID.randomUUID().toString())
        .cData(UUID.randomUUID().toString())
        .build();
  }

}