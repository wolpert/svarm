package com.codeheadsystems.dstore.node.resource;

import static com.codeheadsystems.dstore.node.resource.TraceUUID.TRACE_UUID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraceUUIDTest {

  private static final String UUID = "UUID";
  @Mock private ContainerRequestContext requestContext;
  @Mock private ContainerResponseContext responseContext;
  @Mock private MultivaluedMap<String, Object> multivaluedMap;
  @InjectMocks private TraceUUID traceUUID;

  @Test
  public void roundTrip_notPreSet() throws IOException {
    when(responseContext.getHeaders()).thenReturn(multivaluedMap);

    assertThat(traceUUID.get()).isNull();

    traceUUID.filter(requestContext);
    final String uuid = traceUUID.get();
    assertThat(uuid).isNotNull();

    traceUUID.filter(requestContext, responseContext);
    assertThat(traceUUID.get()).isNull();
    verify(multivaluedMap).add(TRACE_UUID_HEADER, uuid);
  }

  @Test
  public void roundTrip_preSet() throws IOException {
    when(responseContext.getHeaders()).thenReturn(multivaluedMap);
    when(requestContext.getHeaderString(TRACE_UUID_HEADER)).thenReturn(UUID);

    assertThat(traceUUID.get()).isNull();

    traceUUID.filter(requestContext);
    final String uuid = traceUUID.get();
    assertThat(uuid).isNotNull().isEqualTo(UUID);

    traceUUID.filter(requestContext, responseContext);
    assertThat(traceUUID.get()).isNull();
    verify(multivaluedMap).add(TRACE_UUID_HEADER, uuid);
  }

}