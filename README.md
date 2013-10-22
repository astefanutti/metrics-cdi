CDI Extension for Metrics
===========

[CDI](http://www.cdi-spec.org/) portable extension for [Yammer's Metrics](http://metrics.codahale.com/) compliant
with [JSR 346: Contexts and Dependency Injection for Java<sup>TM</sup> EE 1.1](http://jcp.org/en/jsr/detail?id=346).

## About

_Metrics CDI_ provides support for the [_Metrics_ annotations](https://github.com/codahale/metrics/tree/master/metrics-annotation)
in [CDI 1.1](http://jcp.org/en/jsr/detail?id=346) enabled environments.
It implements the contract specified by these annotations with the following level of functionality:
+ Intercept invocations of managed bean methods annotated with
  [`@ExceptionMetered`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/ExceptionMetered.html),
  [`@Metered`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Gauge.html) and
  [`@Timed`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Timed.html))
+ Register the associated [`Metric`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Metric.html) instances
  in the `MetricRegistry` bean available in the CDI container
+ Register [`Gauge`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Gauge.html) instances
  for bean methods annotated with [`@Gauge`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/annotation/Gauge.html)
+ Inject by name [`Gauge`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Gauge.html),
  [`Meter`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Meter.html) and
  [`Timer`](http://maginatics.github.io/metrics/apidocs/com/codahale/metrics/Timer.html) instances

_Metrics CDI_ is compatible with _Metrics_ version 3.0.

## Getting Started

### Using Maven

Add the `metrics-cdi` library as a dependency:
```xml
<dependencies>
    <dependency>
        <groupId>fr.stefanutti</groupId>
        <artifactId>metrics-cdi</artifactId>
        <version>${metrics.cdi.version}</version>
    </dependency>
</dependencies>
```

### Required Dependencies

Besides depending on _Metrics_ (`metrics-core` and `metrics-annotation` modules), _Metrics CDI_ requires
a [CDI 1.1](http://jcp.org/en/jsr/detail?id=346) enabled environment.

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

...
public class TimedMethodBean {

    @Timed(name = "timerName")
    public void timedMethod() {
    }
}
```

### _Metrics_ Registry Resolution

## Limitations

License
-------

Copyright (c) 2013 Antonin Stefanutti

Published under Apache Software License 2.0, see LICENSE