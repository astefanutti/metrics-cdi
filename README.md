CDI Extension for Metrics
===========

[![Build Status][Travis badge]][Travis build] [![Coverage Status][Coveralls badge]][Coveralls build] [![Dependency Status][VersionEye badge]][VersionEye build] [![Trend][Bitdeli Badge]][Bitdeli link]

[Travis badge]: https://secure.travis-ci.org/astefanutti/metrics-cdi.png
[Travis build]: http://travis-ci.org/astefanutti/metrics-cdi
[Coveralls badge]: https://coveralls.io/repos/astefanutti/metrics-cdi/badge.png?branch=master
[Coveralls build]: https://coveralls.io/r/astefanutti/metrics-cdi?branch=master
[VersionEye badge]: https://www.versioneye.com/user/projects/52a633be632bacbded00001c/badge.png
[VersionEye build]: https://www.versioneye.com/user/projects/52a633be632bacbded00001c
[Bitdeli badge]: https://d2weczhvl823v0.cloudfront.net/astefanutti/metrics-cdi/trend.png
[Bitdeli link]: https://bitdeli.com/free "Bitdeli Badge"

[CDI][] extension for [Metrics][] compliant with [JSR 346: Contexts and Dependency Injection for Java<sup>TM</sup> EE 1.1][CDI 1.1].

[CDI]: http://www.cdi-spec.org/
[Metrics]: http://metrics.codahale.com/
[CDI 1.0]: http://jcp.org/en/jsr/detail?id=299
[CDI 1.1]: http://jcp.org/en/jsr/detail?id=346

## About

_Metrics CDI_ provides support of the [_Metrics_ annotations][Metrics annotations] in [CDI 1.1][] enabled environments.
It implements the contract specified by these annotations with the following level of functionality:
+ Intercept invocations of bean methods annotated with [`@ExceptionMetered`][], [`@Metered`][] and [`@Timed`][],
+ Create [`Gauge`][] instances for bean methods annotated with [`@Gauge`][],
+ Inject [`Counter`][], [`Histogram`][], [`Meter`][] and [`Timer`][] instances,
+ Register or retrieve the produced [`Metric`][] instances in the declared [`MetricRegistry`][] bean,
+ Declare automatically a default [`MetricRegistry`][] bean if no one exists in the CDI container.

_Metrics CDI_ is compatible with _Metrics_ version 3.0.

[Metrics annotations]: https://github.com/codahale/metrics/tree/master/metrics-annotation
[`@ExceptionMetered`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/ExceptionMetered.html
[`@Metered`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Gauge.html
[`@Timed`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Timed.html
[`Gauge`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Gauge.html
[`@Gauge`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Gauge.html
[`Counter`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Counter.html
[`Histogram`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Histogram.html
[`Meter`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Meter.html
[`Timer`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Timer.html
[`Metric`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Metric.html
[`MetricRegistry`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/MetricRegistry.html

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
a [CDI 1.1][] enabled environment.

### Supported Containers

_Metrics CDI_ is currently successfully tested with the following containers:

| Container        | Version       | Specification | Artifact Id                                 |
| ---------------- | ------------- | ------------- | ------------------------------------------- |
| [Weld SE][]      | `2.1.1.Final` | [CDI 1.1][]   | `org.jboss.weld.se:weld-se-core`            |
| [OpenWebBeans][] | `1.2.1`       | [CDI 1.0][]   | `org.apache.openwebbeans:openwebbeans-impl` |

[Weld SE]: http://weld.cdi-spec.org/
[OpenWebBeans]: http://openwebbeans.apache.org/

## Usage

### The _Metrics_ Annotations

_Metrics_ comes with the [`metrics-annotation`][Metrics annotations] module that contains a series
of annotations ([`@ExceptionMetered`][], [`@Gauge`][], [`@Metered`][] and [`@Timed`][]).
These annotations are supported by _Metrics CDI_ that implements the contract documented in their Javadoc.

For example, a method on a bean can be annotated with the `@Timed` annotation so that its execution
can be monitored using _Metrics_:

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
to register any [`Metric`][] instances produced. For example, it can be declared as a [producer field][]:

```java
import com.codahale.metrics.MetricRegistry;

public final class MetricRegistryFactoryBean {

    @Produces
    @Singleton
    private final MetricRegistry registry = new MetricRegistry();
 }
```

or a [producer method][]:

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
so that it can be injected in any valid injection point, for example, by declaring an [injected field][]:

```java
import com.codahale.metrics.MetricRegistry;

public final class MetricRegistryBean {

     @Inject
     MetricRegistry registry;
 }
```

or by declaring a [bean constructor][]:

```java
import com.codahale.metrics.MetricRegistry;

public final class MetricRegistryBean {

    private final MetricRegistry registry;

    @Inject
    public void MetricRegistryBean(MetricRegistry registry) {
        this.registry = registry;
    }
}
```

[producer field]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#producer_field
[producer method]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#producer_method
[injected field]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#injected_fields
[bean constructor]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#bean_constructors

## Limitations

[CDI 1.1][CDI 1.1 spec] leverages on [Java Interceptors Specification 1.2][] to provide the ability to associate interceptors
to objects via _typesafe_ interceptor bindings. Interceptors are a mean to separate cross-cutting concerns from the business logic
and _Metrics CDI_ is relying on interceptors to implement the support of _Metrics_ annotations in a CDI enabled environment.

[CDI 1.1][CDI 1.1 spec] sets additional restrictions about the type of bean to which an interceptor can be bound. From a _Metrics CDI_ end-user
perspective, that implies that the managed beans to be monitored with _Metrics_ (i.e. having at least one member method annotated
with one of the _Metrics_ annotations) must be _proxyable_ bean types, as defined in [Unproxyable bean types][], that are:
> + Classes which don’t have a non-private constructor with no parameters,
> + Classes which are declared `final`,
> + Classes which have non-static, final methods with public, protected or default visibility,
> + Primitive types,
> + And array types.

[CDI 1.1 spec]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html
[Java Interceptors Specification 1.2]: http://download.oracle.com/otndocs/jcp/interceptors-1_2-mrel2-eval-spec/
[Binding an interceptor to a bean]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#binding_interceptor_to_bean
[Unproxyable bean types]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html#unproxyable

License
-------

Copyright (c) 2013 Antonin Stefanutti

Published under Apache Software License 2.0, see LICENSE