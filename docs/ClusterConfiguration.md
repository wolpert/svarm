# Cluster Configuration

## How entries are added to a cluster

The proxy gets the entry id and calculates the ring hash. The proxy then
retrieves the set of nodes for each location hash. The nodes are contacted in
parallel to add the entry. (See [proxy documentation](./Proxy.md)) for how fault
tolerance and eventual consistency works here. The timestamp from the proxy
request is included with the entry sent to the node. When the node gets the
data, and if the entry already exists, it verifies which entry has the larger
timestamp, storing that entry into the node and discarding the other.

## How entries are searched

There are 3 different ways to get the entry from the cluster. Loading direcetly
based on the id, through a query based on an 'indexed' criteria, or by scanning
the entire cluster.

### By id

The proxy gets the entry id and calculates the ring hash. Cluster state is
identified. The proxy looks up the nodes for the location hashes. If the state
if the cluster is 'in flux', then each nodes 'previous' node is added to the
search space. (See `Adding nodes to the cluster` below). The list of nodes is
searched in parallel. When a quorum of results is reached, the data is returned
to the client. Due to the quorum, missing data searches will take the most time
as more nodes need to be queried.

The search by id can be sped up by only requiring one node to look for in the
metadata. (If the cluster is 'in flux' then 2 nodes are included in the search
space)

### By query

### By scan

## Impact of node updates

### Adding nodes to the cluster

When adding nodes to an existing cluster, the control plane will do the
following, assuming adding X new nodes:

1. Identify which nodes are responsible for the largest range of hashes.
2. Assign each of the new nodes a hash value in the middle of that large groups.
3. Update etcd 'for the new nodes' so the nodes can self-configure itself.
4. When the node configuration is complete, the control plane adds the new nodes
   to the etc cluster configuration. This means the nodes are now available for
   use. The cluster configuration in etcd is also set to 'influx' and marks each
   node internally as 'updating'.
5. When the cluster configuration is updated, nodes verify the data for the
   table, transferring entries across to other nodes as needed based on the
   location hash.
6. When the nodes finished the verification, they mark themselves as complete
   and notify the control plane.
7. When all nodes are complete, the control plane marks the cluster as 'ready'

### Removing nodes planned

### Node failure

## Appendix

### FAQ

#### What is the ring hash?

The ring hash represents the 'native' hash of the id, and the total locations of
the hash in the ring when looking at the replication factor.

Example:

    Hash Space Size: 24
    Hash of id: 0
    Replication factor: 4
    Location Hashes: 0,6,12,18