# SVARM

Multi-tenant key/value datastore. This is a love-letter to DynamoDB.

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
new open-source NoSQL key/value data store the scales linearly and with little
to no maintenance. This multi-tenant datastore can quickly consume workloads in
the thousands of TPS, support data models with single or multiple partitioning
keys, and available indexing on high-cardinality fields. Companies and research
teams can simply provide the hardware, kubernetes instances or docker swarm and
start using today on or off prem.

"Our small lab was able to install svarm within a day and get up and running"
says Professor Utonium at PPG Chem LLC. "Real-time data capture of our ionic
capture array operating at 60khz from our instruments is now possible...
resulting in a transfer data transfer rate of well over 25 Gbs. Good thing our
fibre optic array is working on our single 42U rack!"

Download your copy of Apache 2.0 licensed svarm today!

## FAQ

### Is it done?

No, not yet. But if you want to help, please do!

### Why build yet another key-value data store?

I really like Cassandra. But after years of working with DynamoDB I found it's
management and capabilities of working with high-cardinality datasets simpler
for the development team. DynamoDB ability to scale also is easier, but of
course, AWS hides that complexity with its oncall staff. So building a
self-sustaining cluster where the operator only has to add nodes or instances to
it seemed like a fun project.

Also, I wanted to build something that could seriously scale.

### Why Svarm?

The project is designed so many servers can work together to achieve a highly
scalable datastore... one that scales linearly. The data nodes themselves can
grow and shrink as needed by the collective. 'Swarming' is how I pictur the
nodes working together. Svarm is the Swedish word for 'swarm' and was available
to register. So... there you go.