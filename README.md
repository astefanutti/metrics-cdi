CDI Extension for Metrics
===========
[CDI](http://www.cdi-spec.org/) extension for [Metrics](http://metrics.codahale.com/) compliant
with [JSR 346: Contexts and Dependency Injection for Java<sup>TM</sup> EE 1.1](http://jcp.org/en/jsr/detail?id=346).

[![Build Status](https://secure.travis-ci.org/astefanutti/metrics-cdi.png)](http://travis-ci.org/astefanutti/metrics-cdi) [![Coverage Status](https://coveralls.io/repos/astefanutti/metrics-cdi/badge.png?branch=master)](https://coveralls.io/r/astefanutti/metrics-cdi?branch=master) [![Dependency Status](https://www.versioneye.com/user/projects/52a633be632bacbded00001c/badge.png)](https://www.versioneye.com/user/projects/52a633be632bacbded00001c)

## About

_Metrics CDI_ provides support of the [_Metrics_ annotations](https://github.com/codahale/metrics/tree/master/metrics-annotation)
in [CDI 1.1](http://jcp.org/en/jsr/detail?id=346) enabled environments.
It implements the contract specified by these annotations with the following level of functionality:
+ Intercept invocations of bean methods annotated with
  [`@ExceptionMetered`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/ExceptionMetered.html),
  [`@Metered`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Gauge.html) and
  [`@Timed`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Timed.html),
+ Create [`Gauge`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Gauge.html) instances
  for bean methods annotated with [`@Gauge`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Gauge.html),
+ Inject [`Counter`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Counter.html),
  [`Histogram`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Histogram.html),
  [`Meter`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Meter.html) and
  [`Timer`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Timer.html) instances,
+ Register or retrieve the produced [`Metric`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Metric.html)
  instances in the declared [`MetricRegistry`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/MetricRegistry.html) bean,
+ Automatically declare a default [`MetricRegistry`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/MetricRegistry.html) bean
  if no one exists in the CDI container.

_Metrics CDI_ is compatible with _Metrics_ version 3.0.

## Getting Started

### Using Maven

Add the `metrics-cdi` library as a dependency:

```xml
<dependencies>
    <dependency>
        <groupId>fr.stefanutti.metrics</groupId>
        <artifactId>metrics-cdi</artifactId>
        <version>${metrics.cdi.version}</version>
    </dependency>
</dependencies>
```

### Required Dependencies

Besides depending on _Metrics_ (`metrics-core` and `metrics-annotation` modules), _Metrics CDI_ requires
a [CDI 1.1](http://jcp.org/en/jsr/detail?id=346) enabled environment.

### Supported Containers

_Metrics CDI_ is currently successfully tested with the following containers:

| Container                                       | Version       | Specification                                  | Artifact Id                                 |
| ----------------------------------------------- | ------------- | ---------------------------------------------- | ------------------------------------------- |
| [Weld SE](http://weld.cdi-spec.org/)            | `2.1.0.Final` | [CDI 1.1](http://jcp.org/en/jsr/detail?id=346) | `org.jboss.weld.se:weld-se-core`            |
| [OpenWebBeans](http://openwebbeans.apache.org/) | `1.2.1`       | [CDI 1.0](http://jcp.org/en/jsr/detail?id=299) | `org.apache.openwebbeans:openwebbeans-impl` |

## Usage

### The _Metrics_ Annotations

_Metrics_ comes with the [`metrics-annotation`](https://github.com/codahale/metrics/tree/master/metrics-annotation)
module that contains a series of annotations (
[`@ExceptionMetered`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/ExceptionMetered.html),
[`@Gauge`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Gauge.html),
[`@Metered`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Gauge.html) and
[`@Timed`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Timed.html)).
These annotations are supported by _Metrics CDI_ that implements the contract documented in their Javadoc.

For example, a method on a bean can be annotated with the `@Timed` annotation so that its execution can be monitored using _Metrics_:

```java
import com.codahale.metrics.annotation.Timed;

public class TimedMethodBean {

    @Timed(name = "timerName")
    public void timedMethod() {
    }
}
```

### Metrics Injection and the `@Metric` Annotation

### _Metrics_ Registry Resolution

_Metrics CDI_ gets a contextual instance of the [`MetricRegistry`][] bean declared in the CDI container
to register any [`Metric`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Metric.html) instances
produced. For example, it can be declared as a producer field:

```java
import com.codahale.metrics.MetricRegistry;

    @Produces
    @Singleton
    private final MetricRegistry registry = new MetricRegistry();
 }
```

or a producer method:

```java
import com.codahale.metrics.MetricRegistry;

public final class MetricRegistryFactoryBean {

    @Produces
    @Singleton
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }
}
```

Otherwise, _Metrics CDI_ automatically registers a [`MetricRegistry`][] bean into the CDI container
so that it can be injected in any valid injection point, for example, by declaring an injected field:

```java
import com.codahale.metrics.MetricRegistry;

public final class MetricRegistryBean {

     @Inject
     MetricRegistry registry;
 }
```

or by declaring an initializer method:

```java
import com.codahale.metrics.MetricRegistry;

public final class MetricRegistryBean {

    private final MetricRegistry registry;

    @Inject
    public void MetricRegistry(MetricRegistry registry) {
        this.registry = registry;
    }
}
```

[`MetricRegistry`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/MetricRegistry.html

## Limitations

[CDI 1.1][] leverages on [Java Interceptors Specification 1.2][] to provide the ability to associate interceptors
to objects via _typesafe_ interceptor bindings. Interceptors are a mean to separate cross-cutting concerns from the business logic
and _Metrics CDI_ is relying on interceptors to implement the support of _Metrics_ annotations in a CDI enabled environment.

[CDI 1.1][] sets additional restrictions about the type of bean to which an interceptor can be bound. From a _Metrics CDI_ end-user
perspective, that implies that the managed beans to be monitored with _Metrics_ (i.e. having at least one member method annotated
with one of the _Metrics_ annotations) must be _proxyable_ bean types, as defined in [Unproxyable bean types][], that are:
> + Classes which don’t have a non-private constructor with no parameters,
> + Classes which are declared `final`,
> + Classes which have non-static, final methods with public, protected or default visibility,
> + Primitive types,
> + And array types.

[CDI 1.1]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html
[Java Interceptors Specification 1.2]: http://download.oracle.com/otndocs/jcp/interceptors-1_2-mrel2-eval-spec/
[Binding an interceptor to a bean]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#binding_interceptor_to_bean
[Unproxyable bean types]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#unproxyable

License
-------

Copyright (c) 2013 Antonin Stefanutti

Published under Apache Software License 2.0, see LICENSE