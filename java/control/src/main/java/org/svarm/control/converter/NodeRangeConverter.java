package org.svarm.control.converter;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.svarm.common.config.api.ImmutableMetaData;
import org.svarm.common.config.api.ImmutableNodeTenantResource;
import org.svarm.common.config.api.ImmutableNodeTenantResourceRange;
import org.svarm.common.config.api.ImmutableTenantResource;
import org.svarm.common.config.api.MetaData;
import org.svarm.common.config.api.NodeTenantResourceRange;
import org.svarm.common.config.api.TenantResource;
import org.svarm.control.model.NodeRange;

/**
 * Converts between node ranges.
 */
@Singleton
public class NodeRangeConverter {

  private static final Logger LOGGER = getLogger(NodeRangeConverter.class);

  /**
   * Constructor.
   */
  @Inject
  public NodeRangeConverter() {
    LOGGER.info("NodeRangeConverter()");
  }

  /**
   * Converts to a node tenant resource range.
   *
   * @param tenant    owner.
   * @param resource  the resource.
   * @param nodeRange the range.
   * @return the result.
   */
  public List<NodeTenantResourceRange> toNodeTenantResourceRanges(final String tenant,
                                                                  final String resource,
                                                                  final List<NodeRange> nodeRange) {
    LOGGER.trace("toNodeTenantResourceRanges({},{},{})", tenant, resource, nodeRange);
    final TenantResource tenantResource = ImmutableTenantResource.builder().tenant(tenant).resource(resource).build();
    return nodeRange.stream().map(nr -> toNodeTenantResourceRanges(tenantResource, nr)).collect(Collectors.toList());
  }

  /**
   * Converter for a single node tenant resource range.
   *
   * @param tenantResource to convert.
   * @param nr             the node range.
   * @return the result.
   */
  public NodeTenantResourceRange toNodeTenantResourceRanges(final TenantResource tenantResource,
                                                            final NodeRange nr) {
    final Optional<String> action;
    switch (nr.status()) {
      case NodeRange.STATUS_DELETING -> action = Optional.of(MetaData.ACTION_DELETE);
      case NodeRange.STATUS_REBALANCING -> action = Optional.of(MetaData.ACTION_REBALANCE);
      default -> action = Optional.empty();
    }
    return ImmutableNodeTenantResourceRange.builder()
        .nodeTenantResource(
            ImmutableNodeTenantResource.builder().uuid(nr.nodeUuid()).tenantResource(tenantResource).build())
        .metaData(ImmutableMetaData.builder().hash(nr.hash()).action(action).build())
        .build();
  }

}
