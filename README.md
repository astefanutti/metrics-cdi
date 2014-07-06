CDI Extension for Metrics
===========

[![Build Status][Travis badge]][Travis build] [![Coverage Status][Coveralls badge]][Coveralls build] [![Dependency Status][VersionEye badge]][VersionEye build]

[Travis badge]: https://travis-ci.org/astefanutti/metrics-cdi.svg
[Travis build]: https://travis-ci.org/astefanutti/metrics-cdi
[Coveralls badge]: https://img.shields.io/coveralls/astefanutti/metrics-cdi.svg
[Coveralls build]: https://coveralls.io/r/astefanutti/metrics-cdi
[VersionEye badge]: https://www.versioneye.com/user/projects/52a633be632bacbded00001c/badge.svg
[VersionEye build]: https://www.versioneye.com/user/projects/52a633be632bacbded00001c

[CDI][] extension for [Metrics][] compliant with [JSR 346: Contexts and Dependency Injection for Java<sup>TM</sup> EE 1.1][JSR 346].

[CDI]: http://www.cdi-spec.org/
[Metrics]: http://metrics.codahale.com/
[JSR 346]: https://jcp.org/en/jsr/detail?id=346
[CDI 1.1]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html
[CDI 1.2]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html

## About

_Metrics CDI_ provides support for the [_Metrics_ annotations][Metrics annotations] in [CDI][] enabled environments.
It implements the contract specified by these annotations with the following level of functionality:
+ Intercept invocations of bean methods annotated with `@Counted`, [`@ExceptionMetered`][], [`@Metered`][] and [`@Timed`][],
+ Create [`Gauge`][] instances for bean methods annotated with [`@Gauge`][],
+ Inject [`Counter`][], [`Histogram`][], [`Meter`][] and [`Timer`][] instances,
+ Register or retrieve the produced [`Metric`][] instances in the declared [`MetricRegistry`][] bean,
+ Declare automatically a default [`MetricRegistry`][] bean if no one exists in the CDI container.

_Metrics CDI_ is compatible with _Metrics_ version 3.1.0+.

[Metrics annotations]: https://github.com/dropwizard/metrics/tree/master/metrics-annotation
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
[_Metrics_ registry]: http://metrics.codahale.com/getting-started/#the-registry

## Getting Started

### Using Maven

Add the `metrics-cdi` library as a dependency:

```xml
<dependencies>
    <dependency>
        <groupId>org.stefanutti.metrics.cdi</groupId>
        <artifactId>metrics-cdi</artifactId>
        <version>${metrics.cdi.version}</version>
    </dependency>
</dependencies>
```

### Required Dependencies

Besides depending on _Metrics_ (`metrics-core` and `metrics-annotation` modules), _Metrics CDI_ requires
a [CDI][] enabled environment.

### Supported Containers

_Metrics CDI_ is currently successfully tested with the following containers:

| Container        | Version          | Specification   | Arquillian Container Adapter                |
| ---------------- | ---------------- | --------------- | ------------------------------------------- |
| [Weld SE][]      | `2.2.2.Final`    | [CDI 1.2][]     | `arquillian-weld-se-embedded-1.1`           |
| [Weld EE][]      | `2.2.2.Final`    | [CDI 1.2][]     | `arquillian-weld-ee-embedded-1.1`           |
| [OpenWebBeans][] | `2.0.0-SNAPSHOT` | [CDI 1.1][]     | `owb-arquillian-standalone`                 |
| [Jetty][]        | `9.2.1`          | [Servlet 3.1][] | `arquillian-jetty-embedded-9`               |
| [WildFly][]      | `8.1.0.Final`    | [Java EE 7][]   | `wildfly-arquillian-container-managed`      |

[Weld SE]: http://weld.cdi-spec.org/
[Weld EE]: http://weld.cdi-spec.org/
[OpenWebBeans]: http://openwebbeans.apache.org/
[Jetty]: http://www.eclipse.org/jetty/
[WildFly]: http://www.wildfly.org/
[Servlet 3.1]: https://jcp.org/en/jsr/detail?id=340
[Java EE 7]: https://jcp.org/en/jsr/detail?id=342

## Usage

_Metrics CDI_ automatically registers new [`Metric`][] instances in the [_Metrics_ registry][] resolved
for the CDI application. The instantiation of these new [`Metric`][] instances happens when:
+ A bean containing [_Metrics_ annotations](#metrics-annotations) is instantiated,
+ A bean containing [metrics injection](#metrics-injection) is instantiated.

The [metrics registration](#metrics-registration) mechanism can be used to customized
the [`Metric`][] instances registered.
Besides, the [_Metrics_ registry resolution](#metrics-registry-resolution) mechanism can be used for the application
to provide a custom [`MetricRegistry`].

### _Metrics_ Annotations

_Metrics_ comes with the [`metrics-annotation`][Metrics annotations] module that contains a series
of annotations ([`@ExceptionMetered`][], [`@Gauge`][], [`@Metered`][] and [`@Timed`][]).
These annotations are supported by _Metrics CDI_ that implements the contract documented in their Javadoc.

For example, a method on a bean can be annotated with the `@Timed` annotation so that its execution
can be monitored using _Metrics_:

```java
import com.codahale.metrics.annotation.Timed;

class TimedMethodBean {

    @Timed(name = "timerName")
    void timedMethod() {
    }
}
```

### Metrics Injection

Instances of any [`Metric`][] can be retrieved by declaring an [injected field][], e.g.:

```java
import com.codahale.metrics.Timer;

import javax.inject.Inject;

@Inject
private Timer timer;
```

[`Metric`][] instances can be injected similarly as method parameters of any [initializer method][]
or [bean constructor][], e.g.:

```java
import com.codahale.metrics.Timer;

import javax.inject.Inject;

class TimerBean {

    private final Timer timer;

    @Inject
    private TimerBean(Timer timer) {
       this.timer = timer;
    }
}
```

In order to provide metadata for the [`Metric`][] instantiation and resolution, the injection point can be annotated
with the `@Metric` annotation, e.g.:

```java
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;

import javax.inject.Inject;

@Inject
@Metric(name = "timerName", absolute = true)
private Timer timer;
```

or when using a [bean constructor][]:

```java
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;

import javax.inject.Inject;

class TimerBean {

    private final Timer timer;

    @Inject
    private TimerBean(@Metric(name = "timerName", absolute = true) Timer timer) {
       this.timer = timer;
    }
}
```

[injected field]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#injected_fields
[initializer method]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#initializer_methods
[bean constructor]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#bean_constructors

### Metrics Registration

While _Metrics CDI_ automatically registers [`Metric`][] instances, it may be necessary for an application
to explicitly provide the [`Metric`][] instances to register. For example, to register custom [gauges],
e.g. with a [producer method][]:

```java
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Ratio;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;

import javax.enterprise.inject.Produces;

class GaugeFactoryBean {

    @Produces
    @Metric(name = "cache-hits", absolute = true)
    Gauge<Double> cacheHitRatioGauge(final @Metric(name = "hits", absolute = true) Meter hits,
                                     final @Metric(name = "calls", absolute = true) Timer calls) {
        return new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(hits.getOneMinuteRate(), calls.getOneMinuteRate());
            }
        };
    }
}
```

or to provide particular `Reservoir` implementations to [histograms][], e.g. with a [producer field][]:

```java
import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import com.codahale.metrics.annotation.Metric;

import javax.enterprise.inject.Produces;

@Produces
@Metric(name = "uniform-histogram")
private final Histogram histogram = new Histogram(new UniformReservoir());
```

[gauges]: http://metrics.codahale.com/manual/core/#gauges
[histograms]: http://metrics.codahale.com/manual/core/#histograms
[`Reservoir`]: http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Reservoir.html
[producer field]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#producer_field
[producer method]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#producer_method

### _Metrics_ Registry Resolution

_Metrics CDI_ automatically registers a [`MetricRegistry`][] bean into the CDI container
to register any [`Metric`][] instances produced. That _default_ [`MetricRegistry`][] bean
can be injected using standard CDI [typesafe resolution][], for example, by declaring an [injected field][]:

```java
import com.codahale.metrics.MetricRegistry;

import javax.inject.Inject;

@Inject
private MetricRegistry registry;
```

or by declaring a [bean constructor][]:

```java
import com.codahale.metrics.MetricRegistry;

import javax.inject.Inject;

class MetricRegistryBean {

    private final MetricRegistry registry;

    @Inject
    private MetricRegistryBean(MetricRegistry registry) {
        this.registry = registry;
    }
}
```

Otherwise, _Metrics CDI_ uses any [`MetricRegistry`][] bean declared in the CDI container with
the [built-in _default_ qualifier][] [`@Default`][] so that a _custom_ [`MetricRegistry`][] can be provided.
For example, that _custom_ [`MetricRegistry`][] can be declared as a [producer field][]:

```java
import com.codahale.metrics.MetricRegistry;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Produces
@Singleton
private final MetricRegistry registry = new MetricRegistry();
```

or a [producer method][]:

```java
import com.codahale.metrics.MetricRegistry;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

class MetricRegistryFactoryBean {

    @Produces
    @Singleton
    private MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }
}
```

[typesafe resolution]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#typesafe_resolution
[built-in _default_ qualifier]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#builtin_qualifiers
[`@Default`]: http://docs.oracle.com/javaee/7/api/javax/enterprise/inject/Default.html

## Limitations

[CDI 1.2][] leverages on [Java Interceptors Specification 1.2][] to provide the ability to associate interceptors
to objects via _typesafe_ interceptor bindings. Interceptors are a mean to separate cross-cutting concerns from the business logic
and _Metrics CDI_ is relying on interceptors to implement the support of _Metrics_ annotations in a CDI enabled environment.

[CDI 1.2][] sets additional restrictions about the type of bean to which an interceptor can be bound. From a _Metrics CDI_ end-user
perspective, that implies that the managed beans to be monitored with _Metrics_ (i.e. having at least one member method annotated
with one of the _Metrics_ annotations) must be _proxyable_ bean types, as defined in [Unproxyable bean types][], that are:
> + Classes which don’t have a non-private constructor with no parameters,
> + Classes which are declared `final`,
> + Classes which have non-static, final methods with public, protected or default visibility,
> + Primitive types,
> + And array types.

[Java Interceptors Specification 1.2]: http://download.oracle.com/otndocs/jcp/interceptors-1_2-mrel2-eval-spec/
[Binding an interceptor to a bean]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#binding_interceptor_to_bean
[Unproxyable bean types]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#unproxyable

License
-------

Copyright © 2013-2014, Antonin Stefanutti

Published under Apache Software License 2.0, see LICENSE
