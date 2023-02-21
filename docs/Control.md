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

# Appendix

## Glossary

## FAQ

### What is the effective limits in svarm?

[Talk about limits per table]

[Talk about etcd limits]

[Talk about limits per node]

[Talk about limits in the control plane]

### How do you scale svarm instance beyond current limits?

[Talk about tenant (tenant table) placement]

[Talk about control plane / etcd bifurcation]

### What is the expected maintenance of svarm?

## Future work

* Move key management to it's own project. (See Violet Keys)
