# Control Plane

## Purpose

This document provides the details on how the control plane is intended to
function to manage the data plane. It is the blue-print for the control plane.

## Behavior

The control plane manages the nodes that belong to a resource. As nodes come and
go, the control plane updates a common configuration in etcd with directions for
the other components. Nodes that join the data plane will need to take data from
the nodes already there. Nodes that leave the plane (Scheduled or not) requires
reshuffling of the data in the data plane based on the replication status.

## Consistent hashing: Ring method

The mechanism to assign data to nodes is based on consistent hashing.
([wiki](https://en.wikipedia.org/wiki/Consistent_hashing))
([orig paper](https://www.cs.princeton.edu/courses/archive/fall09/cos518/papers/chash.pdf))
([decomposition](https://medium.com/omarelgabrys-blog/consistent-hashing-beyond-the-basics-525304a12ba))
Essentially, you hash the key to the data you want, lookup in a data structure
which node(s) have the data and query them. The structure exist in a circular
format where queries that go exceed the top values of the structure start at the
beginning again.

Many consistent hashing techniques rely on the client to find the correct node.
The client would hash the data, and the node ids (or other unique fields like IP
addresses) to find which server(s) have the data. For caches where data-misses
are allowed, this works great. For persistent data, the servers also need to be
aware of ring changes, so they can evaluate replication strategy and move data
as needed when servers come on to the ring or leave the ring.

When the servers need to be aware of ring changes, systems use either the gossip
protocol or sync'd datastore like etcd. In both cases servers need to manage the
changes to the servers on the ring, both planned and unplanned. Those changes
will force data replication.

## Control's job

Control serves the following purposes:

1. Enable or disable new resources in a ring. For svarm, these are tenant
   tables.
2. Provide keys to ensure each node/tenant have independent keys.
3. Manage adding/remove nodes from tenant clusters.

Importantly, control itself does not involve itself in the transferring of the
data, or with the interactions of the nodes themselves. It is a generic
maintainer of individual clusters in a shared pool of resources.

### Why not use gossip?

Control indirectly communicates with the data nodes using etcd in a highly
decoupled fashion. Etcd is a small, highly replicated configuration data store
with the ability for clients to watch for changes in the data they get.

A svarm cluster can have thousands of nodes containing millions of tenant
tables. But only a subset of those nodes are used for any given tenant table.
Importantly, one node made be involved in several tenant tables as well. If we
used gossip, a node should only be aware of the other nodes in the tenant table
cluster it belongs to, not all the nodes in the overall svarm. If every node
knew about every single tenant table cluster, it would overwhelm the data store
itself.

### The replication factor

When nodes are added to the tenant resource, they are assigned a hash. The
control service attempts to assign hashes to even out the distribution of the
data each node will need to consume, but this is expected to not be guaranteed.
But each node is given one hash range they will be responsible for.

When getting the hash for the entry being added to the table, we take into
account the replication factor set for the table. We create one entry per
replication factor. The first hash is calculated using the murmur3 algorithm.
The remainder are calculated to evenly distribute the hashes across the
available space in the hashing algorithm. This is designed to be a fast
mechanism to generate values as this will happen for each entry added, queried
updated or deleted from the system. Those hashes are used to help find the node
to place/search for the data.

## Resource creation

When the resource is first created, the replication factor is set, and initial
nodes are assigned. The system will allow less nodes on initial assignment than
the replication factor. This is because if multiple hashes for an entry ends up
being assigned to the same node, that node will still only contain one data
element. The downside is, of course, less replication than desired. If the
replication factor needs to be strict, then the creation request needs to
require a minimum number of nodes.

Once the nodes are selected, etcd is updated and the nodes watching etcd will
auto-configure themselves. The control plane is notified when the nodes are
ready directly via an API call. Once the initial cluster is complete, data can
then be added as needed.

# Cluster Management

This section involves the discussion on how the control plane manages a given
cluster.

## Tenants

* Cluster of resources are treated like state machines.
* The control plane represents the state machine of any given cluster.
* The control plane reacts to state change.
* The configuration service (etc,zookeeper) represent what the cluster should be
  doing.

## Guidelines

### State machine

The cluster itself is simplified. It starts in the initialization state and
spends most of it's life either stable or rebalancing. Resources are freed up
either from a controlled or uncontrolled shutdown due to failure.

![](http://www.plantuml.com/plantuml/dpng/TL71QeKm4BpdAt8kl1_eeJnfAVIglPKUrisg2p6HP6snNzyLQJ1WR-xEpCwCkMU19ElipSV3fxqb7YUvFTctmCAUVq0u1bDDzj5s2o3Pnlk8ruIMSbmJqd47_ZcaFfr0xqaLpt5UF0cPicaGQH4EevK4my3u1vMGBvGbnKqfUwPf5HU_WwcsxuVKEiwxrjLeYPa8unUbxGmN6_qnnLh7bdhm450vvlIjMAbJxI-QLFXrdLVWFUwTWjF7-GK0)

The nodes have the exact same structure. The difference is when a node
is rebalancing

## Node Expansion

### Overview of steps

### Sequence of events

### Data migration details

## Controlled Node Contraction

### Overview of steps

### Sequence of events

### Data migration details

## Uncontrolled Node Contraction (recovery)

### Overview of steps

### Sequence of events

### Data migration details

# Appendix

## Glossary

* **svarm**: A set of clusters, where each cluster is unique to a tenant's
  resource.
* **cluster**: A set of nodes that participate providing service for a tenant's
  resource.
* **tenant resource**: a software component like a database table or a
  notification queue.
* **node**: a single server instance that can participate in serving in multiple
  clusters. The node only manages a single resource type. (Table, for example)
* **proxy**: handles management of finding the correct set of nodes to talk to.
* **control**: manages the process of adding nodes to the svarm and to clusters
  as needed.
* **configuration**: stores data about the resources and nodes in the svarm with
  their responsibilities.

## FAQ

### Why is svarm considered 'hyper-scaled'?

### What is the effective limits in svarm?

[Talk about limits per table]

[Talk about etcd limits]

[Talk about limits per node]

[Talk about limits in the control plane]

### How do you scale svarm instance beyond current limits?

[Talk about tenant (tenant table) placement]

[Talk about control plane / etcd bifurcation]

### What is the expected maintenance of svarm?

Ideally, adding and removing available nodes to the system should be the largest
set of work needed. Planning on availability zone expansion and other data
center tasks is the resource management that is needed.

However, right now a sufficiently scaled system could involve multiple etcd or
configuration subsystems which suggests the need for a drone module that helps
configured multiple control planes. Ideally this would still limit the needs of
active management. Considering the scale available for one control plan is
millions of nodes and tens of millions of databases, we likely can approach that
problem later.

### Does the key work belong in svarm?

No, not really. It belongs in the violet keys / terrapin projects. Or even use
an existing security system / protocol. Keys in svarm was a stop-gap solution
that does need a better long-term plan.