# CDI Extension for Metrics

[![Build Status][Travis badge]][Travis build] [![Coverage Status][Coveralls badge]][Coveralls build] [![Dependency Status][VersionEye badge]][VersionEye build]

[Travis badge]: https://travis-ci.org/astefanutti/metrics-cdi.svg
[Travis build]: https://travis-ci.org/astefanutti/metrics-cdi
[Coveralls badge]: https://img.shields.io/coveralls/astefanutti/metrics-cdi.svg
[Coveralls build]: https://coveralls.io/r/astefanutti/metrics-cdi
[VersionEye badge]: https://www.versioneye.com/user/projects/52a633be632bacbded00001c/badge.svg
[VersionEye build]: https://www.versioneye.com/user/projects/52a633be632bacbded00001c

[CDI][] portable extension for Dropwizard [Metrics][] compliant with [JSR 346: Contexts and Dependency Injection for Java<sup>TM</sup> EE 1.2][JSR 346 1.2].

[CDI]: http://www.cdi-spec.org/
[Metrics]: http://metrics.dropwizard.io/
[JSR 346]: https://jcp.org/en/jsr/detail?id=346
[JSR 346 1.1]: https://jcp.org/aboutJava/communityprocess/final/jsr346/index.html
[JSR 346 1.2]: https://jcp.org/aboutJava/communityprocess/mrel/jsr346/index.html
[CDI 1.1]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html
[CDI 1.2]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html

## About

_Metrics CDI_ provides support for the [_Metrics_ annotations][Metrics annotations] in CDI enabled environments. It implements the contract specified by these annotations with the following level of functionality:
+ Intercept invocations of bean constructors, methods and public methods of bean classes annotated with [`@Counted`][], [`@ExceptionMetered`][], [`@Metered`][] and [`@Timed`][],
+ Create [`Gauge`][] and [`CachedGauge`][] instances for bean methods annotated with [`@Gauge`][] and [`@CachedGauge`][] respectively,
+ Inject [`Counter`][], [`Gauge`][], [`Histogram`][], [`Meter`][] and [`Timer`][] instances,
+ Register or retrieve the produced [`Metric`][] instances in the resolved [`MetricRegistry`][] bean,
+ Declare automatically a default [`MetricRegistry`][] bean if no one exists in the CDI container.

_Metrics CDI_ is compatible with _Metrics_ version `3.1.0`+.

[Metrics annotations]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/annotation/package-summary.html
[`@ExceptionMetered`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/annotation/ExceptionMetered.html
[`@Counted`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/annotation/Counted.html
[`@Gauge`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/annotation/Gauge.html
[`@Metered`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/annotation/Gauge.html
[`@Timed`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/annotation/Timed.html
[`CachedGauge`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/CachedGauge.html
[`Counter`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/Counter.html
[`Gauge`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/Gauge.html
[`Histogram`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/Histogram.html
[`Meter`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/Meter.html
[`Metric`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/Metric.html
[`Timer`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/Timer.html
[`MetricRegistry`]: https://dropwizard.github.io/metrics/3.1.0/apidocs/com/codahale/metrics/MetricRegistry.html

## Getting Started

#### Using Maven

Add the `metrics-cdi` library as a dependency:

```xml
<dependencies>
    <dependency>
        <groupId>io.astefanutti.metrics.cdi</groupId>
        <artifactId>metrics-cdi</artifactId>
        <version>${metrics.cdi.version}</version>
    </dependency>
</dependencies>
```

#### Required Dependencies

Besides depending on _Metrics_ (`metrics-core` and `metrics-annotation` modules), _Metrics CDI_ requires a CDI enabled environment running in Java 7 or greater.

#### Supported Containers

_Metrics CDI_ is currently successfully tested with the following containers:

| Container        | Version       | Specification          | Arquillian Container Adapter           |
| ---------------- | ------------- | ---------------------- | -------------------------------------- |
| [Weld SE][]      | `2.2.4.Final` | [CDI 1.2][JSR 346 1.2] | `arquillian-weld-se-embedded-1.1`      |
| [Weld EE][]      | `2.2.4.Final` | [CDI 1.2][JSR 346 1.2] | `arquillian-weld-ee-embedded-1.1`      |
| [OpenWebBeans][] | `2.0.0`       | [CDI 1.1][JSR 346 1.1] | `owb-arquillian-standalone`            |
| [Jetty][]        | `9.2.2`       | [Servlet 3.1][]        | `arquillian-jetty-embedded-9`          |
| [WildFly][]      | `8.1.0.Final` | [Java EE 7][]          | `wildfly-arquillian-container-managed` |

[Weld SE]: http://weld.cdi-spec.org/
[Weld EE]: http://weld.cdi-spec.org/
[OpenWebBeans]: http://openwebbeans.apache.org/
[Jetty]: http://www.eclipse.org/jetty/
[WildFly]: http://www.wildfly.org/
[Servlet 3.1]: https://jcp.org/en/jsr/detail?id=340
[Java EE 7]: https://jcp.org/en/jsr/detail?id=342

## Usage

_Metrics CDI_ activates the [_Metrics_ AOP Instrumentation](#metrics-aop-instrumentation) for beans annotated with [_Metrics_ annotations][Metrics annotations] and automatically registers the corresponding `Metric` instances in the [_Metrics_ registry][] resolved for the CDI application. The registration of these `Metric` instances happens each time such a bean gets instantiated. Besides, `Metric` instances can be retrieved from the _Metrics_ registry by declaring [metrics injection points](#metrics-injection).

The [metrics registration](#metrics-registration) mechanism can be used to customize the `Metric` instances that get registered. Besides, the [_Metrics_ registry resolution](#metrics-registry-resolution) mechanism can be used for the application to provide a custom [`MetricRegistry`] instance.

[_Metrics_ registry]: https://dropwizard.github.io/metrics/3.1.0/getting-started/#the-registry

#### Metrics AOP Instrumentation

_Metrics_ comes with the [`metrics-annotation`][] module that contains a set of annotations and provides a standard way to integrate _Metrics_ with frameworks supporting Aspect Oriented Programming (AOP). These annotations are supported by _Metrics CDI_ that implements their contract as documented in their Javadoc.

[`metrics-annotation`]: https://github.com/dropwizard/metrics/tree/master/metrics-annotation

For example, a method of a bean can be annotated so that its execution can be monitored using _Metrics_:

```java
import com.codahale.metrics.annotation.Timed;

class TimedMethodBean {

    @Timed
    void timedMethod() {
        // Timer name => TimedMethodBean.timedMethod
    }
}
```

or the [bean class][] can be annotated directly so that all its public methods get monitored:

```java
import com.codahale.metrics.annotation.Metered;

@Metered
public class MeteredClassBean {

    public void meteredMethod() {
        // Meter name => MeteredClassBean.meteredMethod
    }
}
```

or the [bean constructor][] can be annotated so that its instantiations get monitored:

```java
import com.codahale.metrics.annotation.Counted;

class CountedConstructorBean {

    @Counted
    CountedConstructorBean() {
        // Counter name => CountedConstructorBean.CountedConstructorBean
    }
}
```
The `name` and `absolute` attributes available on every _Metrics_ annotation can be used to customize the name of the `Metric` instance that gets registered in the _Metrics_ registry. The default naming convention being the annotated member simple name relative to the declaring class fully qualified name as illustrated in the above examples.

[bean class]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#what_classes_are_beans
[bean constructor]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#bean_constructors

#### Metrics Injection

`Metric` instances can be retrieved from the _Metrics_ registry by declaring an [injected field][], e.g.:

```java
import com.codahale.metrics.Timer;

import javax.inject.Inject;

class TimerBean {

    @Inject
    private Timer timer; // Timer name => TimerBean.Timer
}
```

`Metric` instances can be injected similarly as parameters of any [initializer method][] or [bean constructor][], e.g.:

```java
import com.codahale.metrics.Timer;

import javax.inject.Inject;

class TimerBean {

    private final Timer timer;

    @Inject
    private TimerBean(Timer timer) { // Timer name => TimerBean.Timer
       this.timer = timer;
    }
}
```

In the above example, Java 8 with the `-parameters` compiler option activated is required to get access to injected parameter name. Indeed, access to parameter names at runtime has been introduced with [JEP-118][]. More information can be found in [Obtaining Names of Method Parameters][] from the Java tutorials. To work around that limitation for Java versions prior to Java 8, or to declare a specific name, the `@Metric` annotation can be used as documented hereafter.

[JEP-118]: http://openjdk.java.net/jeps/118
[Obtaining Names of Method Parameters]: http://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html

In order to provide metadata for the `Metric` instantiation and resolution, the injection point can be annotated with the `@Metric` annotation, e.g., with an [injected field][]:

```java
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;

import javax.inject.Inject;

@Inject
@Metric(name = "timerName", absolute = true)
private Timer timer; // Timer name => timerName
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
        // Timer name => timerName
        this.timer = timer;
    }
}
```

[injected field]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#injected_fields
[initializer method]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#initializer_methods

#### Metrics Registration

While _Metrics CDI_ automatically registers `Metric` instances during the [_Metrics_ AOP instrumentation](#metrics-aop-instrumentation), it may be necessary for an application to explicitly provide the `Metric` instances to register. For example, to provide particular `Reservoir` implementations to [histograms][] or [timers][], e.g. with a [producer field][]:

```java
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.inject.Produces;

class TimedMethodBean {

    @Produces
    @Metric(name = "customTimer") // Timer name => TimedMethodBean.customTimer
    Timer Timer = new Timer(new SlidingTimeWindowReservoir(1L, TimeUnit.MINUTES));

    @Timed(name = "customTimer")
    void timedMethod() {
        // Timer name => TimedMethodBean.customTimer
    }
}
```

Another use case is to register custom [gauges], e.g. with a [producer method][]:

```java
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Ratio;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

class CacheHitRatioBean {

    @Inject
    private Meter hits; // Meter name => CacheHitRatioBean.hits

    @Timed(name = "calls")
    public void cachedMethod() {
        // Timer name => CacheHitRatioBean.calls
        if (hit)
            hits.mark();
    }

    @Produces
    @Metric(name = "cache-hits")
    private Gauge<Double> cacheHitRatioGauge(final Meter hits, final Timer calls) {
        return new RatioGauge() { // Gauge name => CacheHitRatioBean.cache-hits
            @Override
            protected Ratio getRatio() {
                return Ratio.of(hits.getOneMinuteRate(), calls.getOneMinuteRate());
            }
        };
    }
}
```

Since Java 8, [lambda expressions][] can be used as a generic way to compose metrics, so that the above example can be rewritten the following way:

```java
class CacheHitRatioBean {

    @Inject
    private Meter hits;

    @Timed(name = "calls")
    public void cachedMethod() {
        if (hit)
            hits.mark();
    }

    @Produces
    @Metric(name = "cache-hits")
    private Gauge<Double> cacheHitRatioGauge(Meter hits, Timer calls) {
        return () -> calls.getOneMinuteRate() != 0 ?
                     hits.getOneMinuteRate() / calls.getOneMinuteRate() :
                     Double.NaN;
    }
}
```

[gauges]: https://dropwizard.github.io/metrics/3.1.0/manual/core/#gauges
[histograms]: https://dropwizard.github.io/metrics/3.1.0/manual/core/#histograms
[timers]: https://dropwizard.github.io/metrics/3.1.0/manual/core/#timers
[producer field]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#producer_field
[producer method]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#producer_method
[lambda expressions]: http://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html

#### Metrics Registry Resolution

_Metrics CDI_ automatically registers a `MetricRegistry` bean into the CDI container to register any `Metric` instances produced. That _default_ `MetricRegistry` bean can be injected using standard CDI [typesafe resolution][], for example, by declaring an [injected field][]:

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

Otherwise, _Metrics CDI_ uses any `MetricRegistry` bean declared in the CDI container with the [built-in _default_ qualifier][] `@Default` so that a _custom_ `MetricRegistry` can be provided. For example, that _custom_ `MetricRegistry` can be declared with a [producer field][]:

```java
import com.codahale.metrics.MetricRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@Produces
@ApplicationScoped
private final MetricRegistry registry = new MetricRegistry();
```

or with a [producer method][]:

```java
import com.codahale.metrics.MetricRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

class MetricRegistryFactoryBean {

    @Produces
    @ApplicationScoped
    private MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }
}
```

[typesafe resolution]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#typesafe_resolution
[built-in _default_ qualifier]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#builtin_qualifiers

## Limitations

[CDI 1.2][] leverages on [Java Interceptors Specification 1.2][] to provide the ability to [associate interceptors to beans][Binding an interceptor to a bean] via _typesafe_ interceptor bindings. Interceptors are a mean to separate cross-cutting concerns from the business logic and _Metrics CDI_ is relying on interceptors to implement the support of _Metrics_ annotations in a CDI enabled environment.

[CDI 1.2][] sets additional restrictions about the type of bean to which an interceptor can be bound. From a _Metrics CDI_ end-user perspective, that implies that the managed beans to be monitored with _Metrics_ (i.e. having at least one member method annotated with one of the _Metrics_ annotations) must be _proxyable_ bean types, as defined in [Unproxyable bean types][], that are:
> + Classes which don’t have a non-private constructor with no parameters,
> + Classes which are declared `final`,
> + Classes which have non-static, final methods with public, protected or default visibility,
> + Primitive types,
> + And array types.

[Java Interceptors Specification 1.2]: http://download.oracle.com/otndocs/jcp/interceptors-1_2-mrel2-eval-spec/
[Binding an interceptor to a bean]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#binding_interceptor_to_bean
[Unproxyable bean types]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#unproxyable

## License

Copyright © 2013-2014, Antonin Stefanutti

Published under Apache Software License 2.0, see LICENSE
