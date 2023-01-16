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

#### :node:run

If you run into problems when trying to run :node:run, remove

* /tmp/dataNodeConfig.json: This is the configuration file with local keys.
* /tmp/nodeInternalDb: this is the directory with the node's database.
```shell
rm -rf /tmp/dataNodeConfig.json /tmp/nodeInternalDb
```

#### AES Failure

* If you deleted your configuration, the data is lost. Remember to replace all databases.
* Shows up as grabbing the connection just hangs.

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

## Logging

* All constructors that are injected are logged at INFO.
* Any table creation commands are logged at INFO since those are fairly important.
* Customers actions are logged at debug.
* Every internal public method for the classes are logged at trace.
* Adding extra logs as needed, but when in doubt, consider metrics.

## Testing with curl
```shell
curl -v -X PUT 'http://localhost:8080/v1/tenant/customer01'
curl -v -X PUT 'http://localhost:8080/v1/tenant/customer01/table/testtable?primaryKey=fred'
curl -v -X PUT  -H "Content-Type: application/json" \
    -d '{"name": "a test field", "something": 55443}' \
     'http://localhost:8080/v1/tenant/customer01/table/testtable/entry/0001'
curl -v 'http://localhost:8080/v1/tenant/customer01/table/testtable/entry/0001'
curl -v -X DELETE 'http://localhost:8080/v1/tenant/customer01/table/testtable/entry/0001'
curl -v -X DELETE 'http://localhost:8080/v1/tenant/customer01/table/testtable'
curl -v -X DELETE 'http://localhost:8080/v1/tenant/customer01'
```