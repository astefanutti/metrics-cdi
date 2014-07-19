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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.CachedGauge;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Singleton
/* packaged-private */ final class MetricNameHelper {

    private final MetricNameStrategy strategy;

    @Inject
    private MetricNameHelper(MetricNameStrategy strategy) {
        this.strategy = strategy;
    }

    String counterName(Method method) {
        return metricName(method, Counted.class);
    }

    String meterName(Method method, boolean isExceptionMetered) {
        return metricName(method, isExceptionMetered ? ExceptionMetered.class : Metered.class);
    }

    String timerName(Method method) {
        return metricName(method, Timed.class);
    }

    String metricName(AnnotatedMember<?> annotatedMember) {
        return metricName(annotatedMember, annotatedMember.getJavaMember());
    }

    String metricName(InjectionPoint point) {
        return metricName(point.getAnnotated(), point.getMember());
    }

    private String metricName(Method method, Class<? extends Annotation> type) {
        if (method.isAnnotationPresent(type)) {
            Annotation annotation = method.getAnnotation(type);
            return metricName(method, type, metricName(annotation), isMetricAbsolute(annotation));
        } else {
            Class<?> bean = method.getDeclaringClass();
            if (bean.isAnnotationPresent(type) && Modifier.isPublic(method.getModifiers())) {
                Annotation annotation = bean.getAnnotation(type);
                return metricName(bean, method, type, metricName(annotation), isMetricAbsolute(annotation));
            }
        }
        return null;
    }

    private String metricName(Method method, Class<? extends Annotation> type, String name, boolean absolute) {
        String metric = name.isEmpty() ? defaultName(method, type) : strategy.resolve(name);
        return absolute ? metric : MetricRegistry.name(method.getDeclaringClass(), metric);
    }

    private String metricName(Class<?> bean, Method method, Class<? extends Annotation> type, String name, boolean absolute) {
        String metric = name.isEmpty() ? bean.getSimpleName() : strategy.resolve(name);
        return absolute ? MetricRegistry.name(metric, defaultName(method, type)) : MetricRegistry.name(bean.getPackage().getName(), metric, defaultName(method, type));
    }

    private String defaultName(Method method, Class<? extends Annotation> type) {
        if (ExceptionMetered.class.equals(type))
            return MetricRegistry.name(method.getName(), ExceptionMetered.DEFAULT_NAME_SUFFIX);
        else
            return method.getName();
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

    private String metricName(Annotation annotation) {
        if (CachedGauge.class.isInstance(annotation))
            return ((CachedGauge) annotation).name();
        else if (Counted.class.isInstance(annotation))
            return ((Counted) annotation).name();
        else if (ExceptionMetered.class.isInstance(annotation))
            return ((ExceptionMetered) annotation).name();
        else if (Gauge.class.isInstance(annotation))
            return ((Gauge) annotation).name();
        else if (Metered.class.isInstance(annotation))
            return ((Metered) annotation).name();
        else if (Timed.class.isInstance(annotation))
            return ((Timed) annotation).name();
        else
            throw new IllegalArgumentException("Unsupported Metrics annotation [" + annotation.getClass().getName() + "]");
    }

    private  boolean isMetricAbsolute(Annotation annotation) {
        if (CachedGauge.class.isInstance(annotation))
            return ((CachedGauge) annotation).absolute();
        else if (Counted.class.isInstance(annotation))
            return ((Counted) annotation).absolute();
        else if (ExceptionMetered.class.isInstance(annotation))
            return ((ExceptionMetered) annotation).absolute();
        else if (Gauge.class.isInstance(annotation))
            return ((Gauge) annotation).absolute();
        else if (Metered.class.isInstance(annotation))
            return ((Metered) annotation).absolute();
        else if (Timed.class.isInstance(annotation))
            return ((Timed) annotation).absolute();
        else
            throw new IllegalArgumentException("Unsupported Metrics annotation [" + annotation.getClass().getName() + "]");
    }
}