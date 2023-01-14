# Java

Notes about the Java code.

## Build

Requires
* JDK17
* Gradle (Tested with 7.6)

```shell
gradle clean build test
gradle :node:run
```

### Troubleshooting

If you run into problems when trying to run :node:run, remove

* /tmp/dataNodeConfig.json: This is the configuration file with local keys.
* /tmp/nodeInternalDb: this is the directory with the node's database.
```shell
rm -rf /tmp/dataNodeConfig.json /tmp/nodeInternalDb
```
## Package layout.

Layout is based on the Managed Model format updated for Dropwizard/Dagger friendliness. Flat hierarchy. Packages are as
follows;

* **api**: Classes used for communications with upstream clients that call the service.
* **component**: Dagger components for setup. (May be combined later into one Dagger package with module.)
* **converter**: Used to convert objects from upstream or downstream dependencies so managers/engines only needs to know about internal model objects.
* **dao/accessor**: Used to communicate with downstream dependencies. (May be unified into 'accessors' only)
* **engine**: Shared business logic. Engines can be used by many managers and/or resources.
* **factory**: Builders for individual object instances that need care and feeding.
* **healthchecks**: Dropwizard bucket for health checks.
* **manager**: Business logic internal to application. Managers should not call other managers, but can if it makes sense.
* **model**: Internal structure of data.
* **module**: Dagger modules used for IoC/Injection.
* **resources**: How upstream clients call into the dropwizard application. They do conversion of the data as needed and call the proper manager.