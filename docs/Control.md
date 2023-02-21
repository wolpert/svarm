# Control Plane

## Purpose

This document provides the details on how the control plane is intended to
function to manage the data plane. It is the blue-print for the control 
plane.

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
are allowed, this works great. For persistent data, the servers also need to
be aware of ring changes, so they can evaluate replication strategy and move
data as needed when servers come on to the ring or leave the ring.

When the servers need to be aware of ring changes, systems use either the 
gossip protocol or sync'd datastore like etcd. In both cases servers need to
manage the changes to the servers on the ring, both planned and unplanned. Those
changes will force data replication.

### Adding nodes




# Appendix

## Glossary

## FAQ

