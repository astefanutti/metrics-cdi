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
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;
import java.lang.reflect.Member;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@Alternative
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
/* packaged-private */ class MetricProducer {

    private static final Pattern expression = Pattern.compile("[#|$]\\{(.*)\\}");

    @Produces
    private Counter produceCounter(MetricRegistry registry, InjectionPoint point, BeanManager manager) {
        return registry.counter(metricName(point, manager));
    }

    // TODO: produce gauge that have been registered in the Metrics registry

    @Produces
    private Histogram produceHistogram(MetricRegistry registry, InjectionPoint point, BeanManager manager) {
        return registry.histogram(metricName(point, manager));
    }

    @Produces
    private Meter produceMeter(MetricRegistry registry, InjectionPoint point, BeanManager manager) {
        return registry.meter(metricName(point, manager));
    }

    @Produces
    private Timer produceTimer(MetricRegistry registry, InjectionPoint point, BeanManager manager) {
        return registry.timer(metricName(point, manager));
    }

    /* packaged-private */ static String metricName(AnnotatedMember<?> annotatedMember, BeanManager manager) {
        return metricName(annotatedMember, annotatedMember.getJavaMember(), manager);
    }

    private static String metricName(InjectionPoint point, BeanManager manager) {
        return metricName(point.getAnnotated(), point.getMember(), manager);
    }

    private static String metricName(Annotated annotated, Member member, BeanManager manager) {
        if (annotated.isAnnotationPresent(Metric.class)) {
            Metric metric = annotated.getAnnotation(Metric.class);
            if (metric.name().isEmpty()) {
                String name = member.getName();
                return metric.absolute() ? name : MetricRegistry.name(member.getDeclaringClass(), name);
            } else {
                Matcher matcher = expression.matcher(metric.name());
                if (matcher.matches()) {
                    // TODO: add support for EL evaluation
                    return null;
                } else {
                    return metric.absolute() ? metric.name() : MetricRegistry.name(member.getDeclaringClass(), metric.name());
                }
            }
        } else {
            return MetricRegistry.name(member.getDeclaringClass(), member.getName());
        }
    }
}