# Distributed Store

Multi-tenant key/value datastore. This is a love-letter to DynamoDB.

## Design Doc

The project is a work-in-progress. For details, see [the design](./docs/Design.md)

***

# Work In Progress
What follows is simply a way to convey what the project does and
why you should be interested in it. It is **not** done. When it is ready
for public consumption the work in progress note will be removed.

***


## Press Release

October 25, 2025. Scottsdale, Arizona: CodeHead Systems is proud to
present a new open-source NoSQL key/value data store the scales linearly and with
little to no maintenance. This multi-tenant datastore can quickly consume
workloads in the thousands of TPS, support data models with single or 
multiple partitioning keys, and available indexing on high-cardinality fields.
Companies and research teams can simply provide the hardware, kubernetes 
instances or docker swarm and start using today on or off prem.

"Our small lab was able to install dStore within a day and get up and running"
says Professor Utonium at PPG Chem LLC. "Real-time data capture of our ionic capture
array operating at 60khz from our instruments is now possible... resulting in a transfer
data transfer rate of well over 25 Gbs. Good thing our fibre optic array is working
on our single 42U rack!" 

Download your copy of Apache 2.0 licensed dStore today!
## FAQ

### Is it done?

No, not yet. But if you want to help, please do!

### Why build yet another key-value data store?

I really like Cassandra. But after years of working with DynamoDB
I found it's management and capabilities of working with high-cardinality
datasets simpler for the development team. DynamoDB ability to scale
also is easier, but of course, AWS hides that complexity with its oncall
staff. So building a self-sustaining cluster where the operator only
has to add nodes or instances to it seemed like a fun project.
