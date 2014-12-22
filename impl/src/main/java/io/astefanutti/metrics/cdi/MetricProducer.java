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
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
    private Counter produceCounter(InjectionPoint point) {
        return registry.counter(metricName(point));
    }

    @Produces
    private <T> Gauge<T> produceGauge(InjectionPoint point) {
        final String name = metricName(point);
        // A forwarding Gauge must be be returned as the Gauge creation happens when
        // the declaring bean gets instantiated and the corresponding Gauge can be
        // injected before which leads to producing a null value
        return new Gauge<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T getValue() {
                return ((Gauge<T>) registry.getGauges().get(name)).getValue();
            }
        };
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

    private String metricName(InjectionPoint point) {
        Annotated annotated = point.getAnnotated();
        if (annotated instanceof AnnotatedMember)
            return metricName((AnnotatedMember<?>) annotated);
        else if (annotated instanceof AnnotatedParameter)
            return metricName((AnnotatedParameter<?>) annotated);
        else
            throw new IllegalArgumentException("Unable to retrieve metric name for injection point [" + point + "], only members and parameters are supported");
    }

    private String metricName(AnnotatedMember<?> member) {
        if (member.isAnnotationPresent(Metric.class)) {
            Metric metric = member.getAnnotation(Metric.class);
            String name = (metric.name().isEmpty()) ? member.getJavaMember().getName() : strategy.resolve(metric.name());
            return metric.absolute() ? name : MetricRegistry.name(member.getJavaMember().getDeclaringClass(), name);
        } else {
            return MetricRegistry.name(member.getJavaMember().getDeclaringClass(), member.getJavaMember().getName());
        }
    }

    private String metricName(AnnotatedParameter<?> parameter) {
        if (parameter.isAnnotationPresent(Metric.class)) {
            Metric metric = parameter.getAnnotation(Metric.class);
            String name = (metric.name().isEmpty()) ? getParameterName(parameter) : strategy.resolve(metric.name());
            return metric.absolute() ? name : MetricRegistry.name(parameter.getDeclaringCallable().getJavaMember().getDeclaringClass(), name);
        } else {
            return MetricRegistry.name(parameter.getDeclaringCallable().getJavaMember().getDeclaringClass(), getParameterName(parameter));
        }
    }

    // Let's rely on reflection to retrieve the parameter name until Java 8 is required.
    // To be refactored eventually when CDI SPI integrate JEP-118.
    // See http://openjdk.java.net/jeps/118
    // And http://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html
    private String getParameterName(AnnotatedParameter<?> parameter) {
        try {
            Method method = Method.class.getMethod("getParameters");
            Object[] parameters = (Object[]) method.invoke(parameter.getDeclaringCallable().getJavaMember());
            Object param = parameters[parameter.getPosition()];
            Class<?> Parameter = Class.forName("java.lang.reflect.Parameter");
            if ((Boolean) Parameter.getMethod("isNamePresent").invoke(param))
                return (String) Parameter.getMethod("getName").invoke(param);
            else
                throw new UnsupportedOperationException("Unable to retrieve name for parameter [" + parameter + "], activate the -parameters compiler argument or annotate the injected parameter with the @Metric annotation");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException cause) {
            throw new UnsupportedOperationException("Unable to retrieve name for parameter [" + parameter + "], @Metric annotation on injected parameter is required before Java 8");
        }
    }
}