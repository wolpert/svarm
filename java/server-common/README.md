# Server common

Contains the skeleton needed for our dropwizard services.

Someone nice could convert this to updated dagger buildout via
annotation processing.

## To use

The setup requires a skeleton set of classes currently which mostly
involve dagger generation. This is get the building blocks for the
rest of the application.

### Java code
There needs to be at least 3 Java classes.
1. The dropwizard configuration for your service.
2. The dagger component to generate the dropwizard resources that makes your service.
3. The main application itself. (See Long Term below)

Examples (Where the service is called `YourService`:

```java
public class YourServiceConfiguration extends Configuration {
}

@Component(modules = {
    DropWizardModule.class
})
public interface YourServiceDropWizardComponent extends DropWizardComponent {
}

public class YourService extends Server<ControlConfiguration> {
  public static void main(String[] args) throws Exception {
    new YourService().run(args);
  }

  @Override
  protected DropWizardComponent dropWizardComponent(final YourServiceConfiguration configuration,
                                                    final MetricRegistryModule metricRegistryModule) {
    return DaggerYourServiceDropWizardComponent.builder()
        .metricRegistryModule(metricRegistryModule)
        .build();
  }
}
```

### Dropwizard yaml file

Dropwizard requires a startup configuration which maps to the configuration you declared in Java.
Here is an example:

```yaml
logging:
  level: INFO
  loggers:
    com.codeheadsystems: DEBUG
    com.codeheadsystems.dstore: TRACE
    com.mchange.v2.c3p0: WARN
  appenders:
    - type: console
      logFormat: "%d{HH:mm:ss.SSS} [%thread] [%X{trace}] %-5level %logger{36} - %msg%n"

health:
  healthCheckUrlPaths: [ "/health-check" ]
  healthChecks:
    - name: deadlocks
      type: alive
      critical: true
      initialState: true
      schedule:
        checkInterval: 10s
        downtimeInterval: 2s
        initialDelay: 5s
        failureAttempts: 1
        successAttempts: 2
```

## Long term

The end goal is to have the Main executable build by a dagger
'annotation processor' instead of you extending Server itself.

This is more for aesthetics than anything else. Removing the main
server class reduces the number of classes you have to create by one.
