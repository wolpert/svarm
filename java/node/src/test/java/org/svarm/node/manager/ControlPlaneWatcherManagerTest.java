package org.svarm.node.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.svarm.common.config.api.NodeTenantResourceRange.ACTION_DELETE;

import java.util.Optional;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.api.NodeTenantResource;
import org.svarm.common.config.api.NodeTenantResourceRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.common.config.converter.NodeTenantResourceRangeConverter;
import org.svarm.common.config.engine.Event;
import org.svarm.common.config.engine.WatchEngine;
import org.svarm.common.config.factory.WatchEngineFactory;
import org.svarm.datastore.common.TableDefinition;
import org.svarm.node.model.ImmutableTenantTableIdentifier;
import org.svarm.node.model.NodeInternalConfiguration;
import org.svarm.node.model.TenantTable;
import org.svarm.node.model.TenantTableIdentifier;

@ExtendWith(MockitoExtension.class)
class ControlPlaneWatcherManagerTest {

  private static final String UUID = "uuid";
  private static final String KEY = "key";
  private static final String VALUE = "value";
  private static final String TENANT = "tenant";
  private static final String TABLE = "table";
  private static final TenantTableIdentifier IDENTIFIER = ImmutableTenantTableIdentifier.builder()
      .tenantId(TENANT).tableName(TABLE).build();

  @Mock private WatchEngineFactory watchEngineFactory;
  @Mock private TenantTableManager tenantTableManager;
  @Mock private NodeTenantResourceRangeConverter nodeTenantResourceRangeConverter;
  @Mock private ControlPlaneManager controlPlaneManager;
  @Mock private NodeInternalConfiguration nodeInternalConfiguration;
  @Mock private WatchEngine watchEngine;
  @Mock private Event event;

  @Mock private TenantResource tenantResource;
  @Mock private NodeTenantResourceRange nodeTenantResourceRange;
  @Mock private NodeTenantResource nodeTenantResource;
  @Mock private TenantTable tenantTable;

  @Captor private ArgumentCaptor<Consumer<Event>> consumerArgumentCaptor;

  private ControlPlaneWatcherManager manager;

  @BeforeEach
  void setup() {
    when(nodeInternalConfiguration.uuid()).thenReturn(UUID);
    manager = new ControlPlaneWatcherManager(nodeInternalConfiguration, watchEngineFactory, tenantTableManager, nodeTenantResourceRangeConverter, controlPlaneManager);
  }

  @Test
  void putEvent_newTable(){
    when(event.type()).thenReturn(Event.Type.PUT);
    when(event.key()).thenReturn(KEY);
    when(event.value()).thenReturn(Optional.of(VALUE));
    when(nodeTenantResourceRangeConverter.fromKeyValue(KEY, VALUE)).thenReturn(nodeTenantResourceRange);
    // no action.
    when(nodeTenantResourceRange.nodeTenantResource()).thenReturn(nodeTenantResource);
    when(nodeTenantResource.tenantResource()).thenReturn(tenantResource);
    when(tenantResource.tenant()).thenReturn(TENANT);
    when(tenantResource.resource()).thenReturn(TABLE);
    when(tenantTableManager.create(IDENTIFIER, TableDefinition.V1SingleEntryEngine)).thenReturn(tenantTable);

    manager.handle(event);
    verify(controlPlaneManager).enable(IDENTIFIER);
  }

  @Test
  void putEvent_delete_found(){
    when(event.type()).thenReturn(Event.Type.PUT);
    when(event.key()).thenReturn(KEY);
    when(event.value()).thenReturn(Optional.of(VALUE));
    when(nodeTenantResourceRangeConverter.fromKeyValue(KEY, VALUE)).thenReturn(nodeTenantResourceRange);
    when(nodeTenantResourceRange.action()).thenReturn(Optional.of(ACTION_DELETE));
    when(nodeTenantResourceRange.nodeTenantResource()).thenReturn(nodeTenantResource);
    when(nodeTenantResource.tenantResource()).thenReturn(tenantResource);
    when(tenantResource.tenant()).thenReturn(TENANT);
    when(tenantResource.resource()).thenReturn(TABLE);
    when(tenantTableManager.delete(IDENTIFIER)).thenReturn(true);

    manager.handle(event);
    verify(controlPlaneManager).delete(IDENTIFIER);
  }

  @Test
  void putEvent_delete_notFound(){
    when(event.type()).thenReturn(Event.Type.PUT);
    when(event.key()).thenReturn(KEY);
    when(event.value()).thenReturn(Optional.of(VALUE));
    when(nodeTenantResourceRangeConverter.fromKeyValue(KEY, VALUE)).thenReturn(nodeTenantResourceRange);
    when(nodeTenantResourceRange.action()).thenReturn(Optional.of(ACTION_DELETE));
    when(nodeTenantResourceRange.nodeTenantResource()).thenReturn(nodeTenantResource);
    when(nodeTenantResource.tenantResource()).thenReturn(tenantResource);
    when(tenantResource.tenant()).thenReturn(TENANT);
    when(tenantResource.resource()).thenReturn(TABLE);
    when(tenantTableManager.delete(IDENTIFIER)).thenReturn(false);

    manager.handle(event);
    verify(controlPlaneManager).delete(IDENTIFIER);
  }

  @Test
  void startStop() throws Exception {
    when(watchEngineFactory.watchEngine(eq("node"), eq(UUID), consumerArgumentCaptor.capture()))
        .thenReturn(watchEngine);
    manager.start();
    manager.start();
    manager.stop();
    manager.stop();
    verify(watchEngine, times(1)).close();
  }

}