# The Ring

This project contains the core definition of a ring. Rings size range the entire
size of an integer. The ring contains hosts that have a hash range within the
ring itself. When you add data to the ring, you hash the key and store that in
the host that contains the hashes you calculated.

To achieve replication, we calculate the number of hash values based on the
replication factor. If we want a replication of four, we create 3 more hashes
evenly distributed across the ring. Then save copies across all hosts that make
up the ring itself.

## Hosts

When we create the ring, we add in a set number of hosts, and evenly divide out
the hosts in the ring. When a new host is added, it splits a range by a single
host. The new and old host is in rebalanced mode. The new host queries the key
range for all replicated key hashes. The data is stored from the replicated
hosts and removed from the old host in chunks. Once complete the old host range
is reduce and the new hosts is now available.