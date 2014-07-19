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
import com.codahale.metrics.annotation.Timed;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Singleton
/* packaged-private */ final class MetricResolver {

    private final MetricNameStrategy strategy;

    @Inject
    private MetricResolver(MetricNameStrategy strategy) {
        this.strategy = strategy;
    }

    Metric<CachedGauge> cachedGaugeMethod(Method method) {
        return forMethod(method, CachedGauge.class);
    }

    Metric<Counted> countedMethod(Method method) {
        return forMethod(method, Counted.class);
    }

    Metric<ExceptionMetered> exceptionMeteredMethod(Method method) {
        return forMethod(method, ExceptionMetered.class);
    }

    Metric<Gauge> gaugeMethod(Method method) {
        return forMethod(method, Gauge.class);
    }

    Metric<Metered> meteredMethod(Method method) {
        return forMethod(method, Metered.class);
    }

    Metric<Timed> timedMethod(Method method) {
        return forMethod(method, Timed.class);
    }

    private <T extends Annotation> Metric<T> forMethod(Method method, Class<T> type) {
        if (method.isAnnotationPresent(type)) {
            T annotation = method.getAnnotation(type);
            String name = metricName(method, type, metricName(annotation), isMetricAbsolute(annotation));
            return new DoesHaveMetric<T>(annotation, name);
        } else {
            Class<?> bean = method.getDeclaringClass();
            if (bean.isAnnotationPresent(type) && Modifier.isPublic(method.getModifiers())) {
                T annotation = bean.getAnnotation(type);
                String name = metricName(bean, method, type, metricName(annotation), isMetricAbsolute(annotation));
                return new DoesHaveMetric<T>(annotation, name);
            }
        }
        return new DoesNotHaveMetric<T>();
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
            throw new IllegalArgumentException("Unsupported Metrics forMethod [" + annotation.getClass().getName() + "]");
    }

    private boolean isMetricAbsolute(Annotation annotation) {
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
            throw new IllegalArgumentException("Unsupported Metrics forMethod [" + annotation.getClass().getName() + "]");
    }

    interface Metric<T extends Annotation> {

        boolean isPresent();

        String metricName();

        T metricAnnotation();
    }

    private static class DoesHaveMetric<T extends Annotation> implements Metric<T> {

        private final T annotation;

        private final String name;

        private DoesHaveMetric(T annotation, String name) {
            this.annotation = annotation;
            this.name = name;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public String metricName() {
            return name;
        }

        @Override
        public T metricAnnotation() {
            return annotation;
        }
    }

    private static class DoesNotHaveMetric<T extends Annotation> implements Metric<T> {

        private DoesNotHaveMetric() {
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public String metricName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T metricAnnotation() {
            throw new UnsupportedOperationException();
        }
    }
}