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

Integration tests take additional time. To execute build with full integration
tests, do this:

```shell
gradle clean build test integ
```

and, of course, if you just want to run the integ tests...

```shell
gradle integ
```

### End to end tests

The end to end suite starts up the control and data plane, with needed
subsystems. This will abuse your memory and take a while.

To run the end to end tests, you can simply do

```shell
gradle test -Pe2e
```

This will enable the endToEnd subproject. Else that project is skipped.

Note, when trying to run e2e tests in the intelij instance, you'll need to edit
the configuration since intelij will start ignoring tests labeled integ now. Do
this by adding `-Pe2e` to the params.

Logs for the services in these runs are in `build/docker-logs`.

When end to end tests fail, the `docker compose down` command will not execute from
the endToEnd directory. If you start having problems, issue that command manually
from that directory. (May end up moving the docker start/stop into the test suite)

### Troubleshooting

#### Dependencies

The dependencies for this project are controlled by the dependencies
artifact. (`com.codeheadsystesms:dependencies`). The SNAPSHOT build is
used so it is easy to update dependencies here. For whatever reason, if
the dependencies in the public nexus service is out of date, you can clone
`github.com:wolpert/dependencies` and run `gradle publishToMavenLocal`.

#### :node:run

If you run into problems when trying to run :node:run, remove

* /tmp/dataNodeConfig.json: This is the configuration file with local keys.
* /tmp/nodeInternalDb: this is the directory with the node's database.

```shell
rm -rf /tmp/dataNodeConfig.json /tmp/nodeInternalDb
```

#### AES Failure

* If you deleted your configuration, the data is lost. Remember to replace all
  databases.
* Shows up as grabbing the connection just hangs.

#### Gradle version

There have been issues with plugins like checkstyle if the gradle version is
mismatched. Officially the version in the gradle-wrapper.properties is the
version we should build with. 7.6 as of last checked.

## Package layout.

Layout is based on the Managed Model format updated for Dropwizard/Dagger
friendliness. Flat hierarchy. Packages are as follows;

* **api**: Classes used for communications with upstream clients that call the
  service.
* **component**: Dagger components for setup. (May be combined later into one
  Dagger package with module.)
* **converter**: Used to convert objects from upstream or downstream
  dependencies so managers/engines only needs to know about internal model
  objects.
* **dao/accessor**: Used to communicate with downstream dependencies. (May be
  unified into 'accessors' only)
* **engine**: Shared business logic. Engines can be used by many managers and/or
  resources.
* **factory**: Builders for individual object instances that need care and
  feeding.
* **healthchecks**: Dropwizard bucket for health checks.
* **manager**: Business logic internal to application. Managers should not call
  other managers, but can if it makes sense.
* **model**: Internal structure of data.
* **module**: Dagger modules used for IoC/Injection.
* **resources**: How upstream clients call into the dropwizard application. They
  do conversion of the data as needed and call the proper manager.

## Logging

* All constructors that are injected are logged at INFO.
* Any table creation commands are logged at INFO since those are fairly
  important.
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

## Modules

* common: Stuff that is shared with all code bases.
* common-javaClient: Java client utilities that are shared everywhere.
* config-common: Library to talk to the configuration service.
* control: The control plane service.
* control-common: API code shared from the control plane and its clients.
* control-javaClient: java client for the control plane clients.
* endToEnd: end to end tests. These will take the most time.
* node: The node service.
* node-common: API code shared from the node to its clients.
* node-javaClient: java client for the node clients.
* proxy: The proxy service
* server-common: Common files for all services. (May turn into its own project)
