/**
 * Copyright (C) 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.stefanutti.metrics.cdi;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;

@Singleton
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
/* packaged-private */ final class MetricProducer {

    private final MetricNameHelper helper;

    @Inject
    MetricProducer(MetricNameHelper helper) {
        this.helper = helper;
    }

    @Produces
    private Counter produceCounter(MetricRegistry registry, InjectionPoint point) {
        return registry.counter(helper.metricName(point));
    }

    @Produces
    @SuppressWarnings("unchecked")
    private <T> Gauge<T> produceGauge(MetricRegistry registry, InjectionPoint point) {
        // TODO: As gauge metrics are registered at instantiation time of the annotated
        // beans this may lead to producing null values for that gauge bean in case
        // the gauge metrics get injected before the corresponding beans. A more
        // sophisticated strategy may be designed to delay the retrieval of the
        // underlying gauges from the Metrics registry for example.
        return (Gauge<T>) registry.getGauges().get(helper.metricName(point));
    }

    @Produces
    private Histogram produceHistogram(MetricRegistry registry, InjectionPoint point) {
        return registry.histogram(helper.metricName(point));
    }

    @Produces
    private Meter produceMeter(MetricRegistry registry, InjectionPoint point) {
        return registry.meter(helper.metricName(point));
    }

    @Produces
    private Timer produceTimer(MetricRegistry registry, InjectionPoint point) {
        return registry.timer(helper.metricName(point));
    }
}