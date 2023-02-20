# Control Plane

Provides ability for nodes to get placed into the swarm, connects table requests
to nodes and identifies nodes to proxies.

The control plane will require global locking at some point at the tenant table
level. Zookeeper is a reasonable lock manager when we get there.

The principal here is the control plane should not be specific to svarm, rather
it be generic with any hash-based location system with recovery.

Also, consider regions vs. availability zones. The control plane is for one
region that can have multiple availability zones. Finally, the application
initially does not have anything specific to kubernetes. However, there is no
reason that could not be added. It should just not be required.

## Configuration

### Static

* Region
* Zookeeper lookup

### Dynamic

## Node Registration

To start, nodes self-identify into the swarm on startup. (Just so we can get an
initial setup).

***Current plan: Subject to change***

New nodes should be able to be added to the swam easily and securely. Especially
in an automated fashion. Nodes self-identify with their UUIDs, using the plane's
public key, encrypt their uuid and sign with the (current)
swarm private key. When registering, the control plane decrypts the payload and
verifies the signature. That enables the node to be verified.

Requires:

* Control plane asym keys and swarm keys.
* Rotation and distribution policy.

Other options:

* OAuth2 integration

### Startup Sequence

1. Nodes query the control plane for the status of their UUID.
2. If the result of the status request shows it is enabled, do nothing.
3. If the result is a 404
    1. Register the node. If this fails, kill the node server. (It's probably
       banned.)
    2. Set the status to enabled.
4. If the result is disabled, enable the node.

### Shutdown sequence

When a node is being shutdown, it needs to disable in the control plane.