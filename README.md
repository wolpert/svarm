# SVARM

Hyper-scaled control plane managing a multi-tenant key/value datastore.

This is a love-letter to DynamoDB.

## Purpose

Svarm is a large-scale key/value data store. It consists of components that can
be used in other projects as well. These include full systems such as the
control plane to manage scaling resources in a linear fashion. It also includes
sub-components to build other projects out of like the common server code that
provides opinionated (Dagger, immutables, Dropwizard) framework extending the
great work done by the dropwizard team.

## Intended audience

Svarm is for companies that want to rely on their own data centers for a
reliable, scalable NoSQL data store. In essence, bringing the cloud back home.

## Design Doc

The project is a work-in-progress. For details,
see [the design](./docs/Design.md).

The control plane itself is designed to be usable for large-scale node
management. Details can be found in [this doc](./docs/Control.md).

***

# Work In Progress

What follows is simply a way to convey what the project does and why you should
be interested in it. It is **not** done. When it is ready for public consumption
the work in progress note will be removed.

***

## Press Release

October 25, 2025. Scottsdale, Arizona: CodeHead Systems is proud to present a
new open-source NoSQL key/value datastore that scales linearly, with little
to no maintenance. This multi-tenant datastore can quickly consume workloads in
the tens of thousands of TPS, support data models with single or multiple partitioning
keys, and provides indexing on high-cardinality fields. Companies and research
teams can simply provide the hardware, kubernetes instances or docker swarm and
start using today either on or off prem.

"Our small lab was able to install svarm within a day and get up and running"
says Professor Utonium at PPG Chem LLC. "Real-time data acquisition of our ionic
capture array operating at 60Ghz is now possible...
resulting in a data transfer rate of well over 25 GB/s. Good thing our
fibre optic array with quantum gates is working on our 12 42U racks!"

Download your copy of Apache 2.0 licensed svarm today!

## FAQ

### Is it done?

Lol, no, not yet. But if you want to help, please do!

### Is svarm really about a NoSQL data store?

Yes and no. Svarm was intended to provide a generic control plane for any hyper-scaled
system. I'm proving it out with a key-value data store. But I do like
DynamoDB so why not redo it?

### Why build yet another key-value data store?

I really like Cassandra. But after years of working with DynamoDB I found it's
management and capabilities of working with high-cardinality datasets simpler
for the development team. DynamoDB ability to scale also is easier, but of
course, AWS hides that complexity with its oncall staff. So building a
self-sustaining cluster where the operator only has to add nodes or instances to
it seemed like a fun project.

Also, I wanted to build something that could seriously scale. Something that
companies can use in their own data center.

### Why Svarm?

The project is designed so many servers can work together to achieve a highly
scalable datastore... one that scales linearly. The data nodes themselves can
grow and shrink as needed by the collective. 'Swarming' is how I pictur the
nodes working together. Svarm is the Swedish word for 'swarm' and was available
to register. So... there you go.

### How can I help?

I need folks who can write code, documentation, and build websites. This is
all open-source and benefits everyone. Come join and be part of the experience.