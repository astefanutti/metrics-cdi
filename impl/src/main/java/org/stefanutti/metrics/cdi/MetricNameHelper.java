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
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;
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

    String metricName(AnnotatedMember<?> annotatedMember) {
        return metricName(annotatedMember, annotatedMember.getJavaMember());
    }

    String metricName(InjectionPoint point) {
        return metricName(point.getAnnotated(), point.getMember());
    }

    String timerName(Method method) {
        if (method.isAnnotationPresent(Timed.class)) {
            Timed timed = method.getAnnotation(Timed.class);
            String name = timed.name().isEmpty() ? method.getName() : strategy.resolve(timed.name());
            return timed.absolute() ? name : MetricRegistry.name(method.getDeclaringClass(), name);
        } else {
            Class<?> bean = method.getDeclaringClass();
            if(bean.isAnnotationPresent(Timed.class) && Modifier.isPublic(method.getModifiers())) {
                Timed timed = bean.getAnnotation(Timed.class);
                String name = timed.name().isEmpty() ? bean.getSimpleName() : strategy.resolve(timed.name());
                return timed.absolute() ? MetricRegistry.name(name, method.getName()) : MetricRegistry.name(bean.getPackage().getName(), name, method.getName());
            }
        }
        return null;
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