# dStore

Distributed Storage

# tl;dr

dStore provides an enterprise-grade, federated, multi-tenant 
key/value datastore in the same vein as DynamoDB and Cassandra 
that scales linearly and with little required maintenance.

# Summary

dStore provides for a key/value datastore, designed for those familiar
with DynamoDB and Cassandra. It's built to easily add nodes to the cluster
and internally re-distribute the workload as needed. Designed from the
ground up to allow for multi-tenant usages. 

It would be great if dStore was API compatible with DynamoDB.

# System Components

[![SystemComponents](./dStore-components.png)](https://viewer.diagrams.net/?tags=%7B%7D&highlight=0000ff&edit=_blank&layers=1&nav=1&title=dStoreSystemComponent.drawio#R7VrZctowFP0aHpuxLYzhkSVtZ5q0mTDdnjrCVowa4UuFWNyvr4RlbCOH0NREpBPyEOnoyss551oXmRYazjbvOJ5PryEirOU50aaFRi3Pc512V%2F5TSJohgdvJgJjTSAcVwJj%2BJvlMjS5pRBaVQAHABJ1XwRCShISigmHOYV0NuwNWPescx8QAxiFmJvqVRmKaoV3fKfD3hMbT%2FMyuo0dmOA%2FWwGKKI1iXIHTZQkMOILLWbDMkTJGX85LNe%2FvA6O7COEnEMRMGYhlGn%2Fv9%2FuT2x%2F31l5VDPvx64%2FWyw6wwW%2Bo71lcr0pwCDsskIuooTgsN1lMqyHiOQzW6lqJLbCpmTPZc2dSHI1yQzYMX6u5uX%2FqGwIwInsqQfEJOblq1wrrg3%2B9obFri3g80iLXm8e7QBS2yoZn5C5YQMkghkbSJ7gIXU4ghweyyQAdV2oqYK4C5JusnESLVnsdLAVUqJV08%2FabnbzvfVefCz7ujTXlwlOreQnC437m1EEVd8mFJ5B3CkofkABU6ewXmMREH4rI0NzXmhGFBV9ULaVwwr9Owre8oY0NgwLdz0Z2v%2FhQOiSjh2WenQWmks%2F00lCCdaoIgM0FQUJMg7fapEiSwkR8NujpfWR5ztWfT1K7h6SGj6lb3uZcrzVw1lzPWD4Vy4ED5jcpV7QpPCLuBBRUUEhkyASFgVgroMxqrAQF7GQBLwWgiLZ2vs04zZu5Vvew5NWau8XLnVFbuvXAre0dauW3Vyo4Nlp%2B6oFpQx7eqjvuqzkF1rNY2rlnKvKpTqdatVp7GIn3DYZP%2BW%2FXZRM14xDrres%2B50CKDqREWWCIfISLW%2BUJ7NbZvm672i6LLtc6X%2F6L4su8v8xvzOfOFrPOFrCzDJ93bcY%2Fe3AlsLrGuadVbMpdk0iS2blT%2F7IzqBgZdLa%2FD5GkHEV3JZqyafenCVNBwkY%2FJc5WGa2Zkj4cjg8cCOMnxCd%2BP3J%2F%2FwIZGmDIq1eTocSknme5Xkx2Aw%2Ft464ZP2U5GnlFZqrl%2BQ%2FoHwYVfcUA78A0HdH3TAL1TGcAza9IhyCId1MwbhhP7j%2Ff9rKnb83%2FerPGsbGme9PHutY98vHt29%2B7NSvfs7Brs2dWzblez3D2P752775nnk9ldgypd6dabS967qMvS%2FOVOAmotqbwh0hDWe%2Bmh5IqUd%2BHzTfYZjaLtg6NOh6pSDUhxxGujXo0Q6GQVtGMIEek64b%2BVYO%2FVtl9THda92n6CBrJb%2FLZgO1b6hQa6%2FAM%3D)

dStore has four layers that work together.

- **Proxy**: Provides for client access. Routes the request internally to the correct data node, handling redundancy and failover.
- **Data Node**: Nodes that manage the data itself.
- **Control**: Manages the other nodes in the cluster, replication strategy and joining/merging nodes.
- **Reporting**: Extracts usage patterns from the nodes and proxies.

## Proxy

Proxy handles authentication of the client, finding the correct node to talk
to, and making the requests to the data nodes. For replication, this means
the proxy may be talking to multiple data nodes at once. Those requests may
be async or sync, depending on the requirements from the downstream client.
(Note that the proxy does not work async itself, rather forward to the data
nodes to process the request in an async fashion.)

## Data Node

Data nodes are responsible for the storage/retrieval of the data. When a new
node is added to the system, it has to ask nodes to stream the data being replicated
to this node. (For security reasons, it does not go through the proxies.) It can
manage the requests in an async fashion. It has to also know it's system limits
and can reject work if its resources are full.

Functionally, the data nodes can operate independently of all other components.
But when a data node is added to a control node, it becomes available for a
greater part of the system. Each data node can respond to the core API
requirements.

V1 tables look like this:
- RID_ID: Indexed, first part of the primary composite key. This is the unique identifier
- C_COL:  Indexed, second part of the primary composite key.
- HASH: The hash value of the RID_ID for mgmt.
- C_DATA_TYPE: Enum, either String or Integer.
- C_DATA: Nullable String.

Each data node has a table to describe tables it controls. Example:

- RID_TENANT: Indexed, first part of the primary composite key
- RID_TABLE: Index, second part of the primary composite key
- HASH_START: String hash identifier if there is a min hash key allowed. 
- HASH_END: End hash identifier if there is a max hash key allowed.
- QUANTITY: Estimate number of entries in the table.
- UUID: The tenants UUID specific to this node.
- TABLE_VERSION: The version of table this requires.

One JSON object is broken down into multiple rows in the relational table.
Each store has a separate table.

### UUIDs and Keys

Every component in the cluster has their own UUIDs and 256 bit key.
This includes:

- Proxy
- Control Plane
- Node
- Node per control plane.
- Proxy per control plane.
- Tenant per control plane
- Tenant per node.

These are used at various points for encryption. All encryption is 
AES/GCM/SIV. When we use the word 'key', the keys are identified by the UUID
by are the 256b keys.

### Hashing

The initial key hashing technique will be [Murmur2](https://en.wikipedia.org/wiki/MurmurHash)
which provides good randomness and executes very fast. Collisions are allowed in
the lookup strategy, and the 32bit variant is enough of a namespace for us.

(Note that this is still being decided. Other option was [FNV-1a](https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function)
which does not have the little/big endian issue. The important thing was multiple
platforms and languages give the same results.)

### Data storage

When a node is started up, a 32bit key is created and store locally. When
a node connects to the control plane, a second 32bit key is retrieved from
the control plane. These two keys are XOR together and provides the AES
key for the node internal configuration database.

When a client as added to a node, the key for the client is made up of the
following keys:
- Node
- Tenant based on the node
- Tenant based on the control plane.

The resulting key 

Each node has a unique identifier, which is a UUID. Likewise, the control plane
also supplies an unique identifier (UUID) to the node when it connects.
The two UUIDs are hashed together which generates the AES256 key used to 
encrypt/decrypt the tables. (The mechanism to add a data node to a control plane
will be documented in the control plane flow.)

## Control

The Proxy and Data nodes are managed by the Control plane. Not pictured here is
how new components are added to the control plane. A UI is expected at some point,
but at least initially it will only be CLI based. 

The Control segment allows for new nodes to be added or nodes discontinued. This
also impacts the proxies as they need to know which nodes to access.

## Reporting

The reporting infrastructure pushes data to an external data store. It does not
define the reports required, rather provides the mechanism to export the data out.
Data here includes the utilization of servers from the client side and the storage
that is actually used. The goal is to provide data on client utilization for
dStore service, as well as how the nodes are doing keeping up with demand. But
at this point in the project, the first goal is to provide the data funnel.

# API

## Resource ID

Resource IDs identify structure within dStore and related properties. This
format is a variation of what is found within Amazon's ARN. It represents the
unique identifier of any `resource` within this system.

General format is: 

    rid:service:tenant:location:resource_type:resource_id

So for example, dStore object would generically look like this:

    rid:dStore:<tenant>:<location>:table:<table_name>

And given the tenant id being 1234, the location is NA for the table named
entries, we would have:

    rid:dStore:1234:NA:table:entries

## DSTORE URLS

Vi URLs for dStore are effectively `CRUD` operations. Storage initially is
based on a simple ID for each entry in the table. 
The ID is either String, Integer or Bytes. When you define the table, you have to pick one type for the ID.

### Tenant manipulations

* **List**: HTTP GET /v1/tenant
* **Read**: HTTP GET /v1/tenant/{tenant}
* **Create**: HTTP PUT /v1/tenant/{tenant}
* **Delete**: HTTP DELETE /v1/tenant/{tenant}

### Table manipulations

You can create, delete or list tables. These URLS are as follows:

* **List**: HTTP GET /v1/tenant/{tenant}/table
* **Create**: HTTP PUT /v1/tenant/{tenant}/table/{table}
* **Read**: HTTP GET /v1/tenant/{tenant}/table/{table}
* **Delete**: HTTP DELETE /v1/tenant/{tenant}/table/{table}


### Accessing entries in the table.

* **Create**: HTTP PUT /v1/tenant/{tenant}/table/{table}/id/{id}
* **Read**: HTTP GET /v1/tenant/{tenant}/table/{table}/id/{id}
* **Update**: HTTP POST /v1/tenant/{tenant}/table/{table}/id/{id}
* **Delete**: HTTP DELETE /v1/tenant/{tenant}/table/{table}/id/{id}

In Create/Update requests above, you must supply a body of a message which
includes the data to store.

# Tooling

## Rust
* Rust-built
* [Web Framework Actix](https://crates.io/crates/actix-web) [(ref)](https://kerkour.com/rust-web-framework-2022)
* [IoC]()

## Java

### HSQLDB
hsqldb is used for the database because it's fast and we can add in 
AES/GCM/SIV for the encryption per database. MySQL was considered but
decided to keep it 'in process' database instead of external process.
(MySQL can encrypt per tablespace, which meets the needs here. PostGRESQL
cannot do that in the same way.) Other embedded databases do not support
the encryption we need.

Moving to MySQL is a possibility here, but seriously increases the complexity
of an install. This may be warranted if popularity of the project increases
and someone can demonstrate it will be actually worth it. I would love
PostGRESQL over MySQL if encryption can be maintained correctly.

### Liquibase
There are many databases per server, and two types of databases. The internal
and the tenant type. Liquibase is used when enabling a node.

### HikariCP
[HikariCP](https://github.com/brettwooldridge/HikariCP/) is used for pooling
as it's still being developed and in use.

### Hibernate

Instead of using raw JDBC calls, hibernate is used to simplify development.
Because why not. It's fine. We can always switch to raw JDBC if it's really needed.

### Misc

Dropwizard, Jackson, Dagger, Immutables, Logback, Micrometer, AssertJ, and Jupiter are standard
for me on these types of applications.