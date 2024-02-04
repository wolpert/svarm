# Feature Flag
A minor implementation of Fowler's [feature toggle](https://martinfowler.com/articles/feature-toggles.html) 
designed for enabling and dialing up a feature for a percentage of traffic in
a consistent way. Provides for the following:

* Shared configuration for feature flags across multiple services (etcd, jdbc, etc)
* Quick disablement of a feature
* Ability to dial up a percentage of traffic based on a common identifier.
* Dial up is consistent. (At x% dial up, the same identifier will consistent be enabled or disabled)

If successful, this may go to its own project.

## Why not use an existing library?

* Current libraries are overly complicated or spring-based. (ff4j)
* Too many bells ans whistles and leaky abstractions. 

## TODO
1. Added a watcher on new feature flags so that on change/delete events we invalidate the cache.
2. Generic CLI would be nice.