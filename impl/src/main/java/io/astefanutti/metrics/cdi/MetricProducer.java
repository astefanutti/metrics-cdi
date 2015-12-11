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
package io.astefanutti.metrics.cdi;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;

@Singleton
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
/* package-private */ final class MetricProducer {

    private final MetricRegistry registry;

    private final MetricName metricName;

    @Inject
    private MetricProducer(MetricRegistry registry, MetricName metricName) {
        this.registry = registry;
        this.metricName = metricName;
    }

    // Use to produce and register custom metrics
    void registerMetric(BeanManager manager, Bean<?> bean, AnnotatedMember<?> member) {
        Metric metric = (Metric) manager.getReference(bean, member.getBaseType(), manager.createCreationalContext(bean));
        registry.register(metricName.of(member), metric);
    }

    @Produces
    private Counter produceCounter(InjectionPoint ip) {
        return registry.counter(metricName.of(ip));
    }

    @Produces
    private <T> Gauge<T> produceGauge(InjectionPoint ip) {
        final String name = metricName.of(ip);
        // A forwarding Gauge must be be returned as the Gauge creation happens when
        // the declaring bean gets instantiated and the corresponding Gauge can be
        // injected before which leads to producing a null value
        return new Gauge<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T getValue() {
                // TODO: better error report when the gauge doesn't exist
                return ((Gauge<T>) registry.getGauges().get(name)).getValue();
            }
        };
    }

    @Produces
    private Histogram produceHistogram(InjectionPoint ip) {
        return registry.histogram(metricName.of(ip));
    }

    @Produces
    private Meter produceMeter(InjectionPoint ip) {
        return registry.meter(metricName.of(ip));
    }

    @Produces
    private Timer produceTimer(InjectionPoint ip) {
        return registry.timer(metricName.of(ip));
    }
}