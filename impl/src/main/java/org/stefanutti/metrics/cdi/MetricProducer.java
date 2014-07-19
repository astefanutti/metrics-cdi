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
import com.codahale.metrics.annotation.Metric;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;
import java.lang.reflect.Member;

@Singleton
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
/* packaged-private */ final class MetricProducer {

    private final MetricRegistry registry;

    private final MetricNameStrategy strategy;

    @Inject
    private MetricProducer(MetricRegistry registry, MetricNameStrategy strategy) {
        this.registry = registry;
        this.strategy = strategy;
    }

    @Produces
    private Counter produceCounter(MetricRegistry registry, InjectionPoint point) {
        return registry.counter(metricName(point));
    }

    @Produces
    @SuppressWarnings("unchecked")
    private <T> Gauge<T> produceGauge(InjectionPoint point) {
        // TODO: As gauge metrics are registered at instantiation time of the annotated
        // beans this may lead to producing null values for that gauge bean in case
        // the gauge metrics get injected before the corresponding beans. A more
        // sophisticated strategy may be designed to delay the retrieval of the
        // underlying gauges from the Metrics registry for example.
        return registry.getGauges().get(metricName(point));
    }

    @Produces
    private Histogram produceHistogram(InjectionPoint point) {
        return registry.histogram(metricName(point));
    }

    @Produces
    private Meter produceMeter(InjectionPoint point) {
        return registry.meter(metricName(point));
    }

    @Produces
    private Timer produceTimer(InjectionPoint point) {
        return registry.timer(metricName(point));
    }

    void produceMetric(BeanManager manager, Bean<?> bean, AnnotatedMember<?> member) {
        com.codahale.metrics.Metric metric = (com.codahale.metrics.Metric) manager.getReference(bean, member.getBaseType(), manager.createCreationalContext(null));
        registry.register(metricName(member), metric);
    }

    private String metricName(AnnotatedMember<?> annotatedMember) {
        return metricName(annotatedMember, annotatedMember.getJavaMember());
    }

    private String metricName(InjectionPoint point) {
        return metricName(point.getAnnotated(), point.getMember());
    }

    private String metricName(Annotated annotated, Member member) {
        if (annotated.isAnnotationPresent(Metric.class)) {
            Metric metric = annotated.getAnnotation(Metric.class);
            String name = (metric.name().isEmpty()) ? member.getName() : strategy.resolve(metric.name());
            return metric.absolute() ? name : MetricRegistry.name(member.getDeclaringClass(), name);
        } else {
            return MetricRegistry.name(member.getDeclaringClass(), member.getName());
        }
    }
}