/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.astefanutti.metrics.cdi;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.annotation.CachedGauge;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MetricsExtension implements Extension {

    private static final AnnotationLiteral<Nonbinding> NON_BINDING = new AnnotationLiteral<Nonbinding>(){};

    private static final AnnotationLiteral<InterceptorBinding> INTERCEPTOR_BINDING = new AnnotationLiteral<InterceptorBinding>(){};

    private static final AnnotationLiteral<MetricsBinding> METRICS_BINDING = new AnnotationLiteral<MetricsBinding>(){};

    private static final AnnotationLiteral<Default> DEFAULT = new AnnotationLiteral<Default>(){};

    private final Map<Bean<?>, AnnotatedMember<?>> metrics = new HashMap<>();

    private final Map<Bean<?>, AnnotatedMember<?>> healthChecks = new HashMap<>();

    private final MetricsConfigurationEvent configuration = new MetricsConfigurationEvent();

    @SuppressWarnings("unchecked")
    <T> Optional<T> getParameter(MetricsParameter parameter) {
        return (Optional<T>) Optional.ofNullable(configuration.getParameters().get(parameter));
    }

    private void addInterceptorBindings(@Observes BeforeBeanDiscovery bbd, BeanManager manager) {
        declareAsInterceptorBinding(Counted.class, manager, bbd);
        declareAsInterceptorBinding(ExceptionMetered.class, manager, bbd);
        declareAsInterceptorBinding(Metered.class, manager, bbd);
        declareAsInterceptorBinding(Timed.class, manager, bbd);
    }

    private <X> void metricsAnnotations(@Observes @WithAnnotations({CachedGauge.class, Counted.class, ExceptionMetered.class, Gauge.class, Metered.class, Timed.class}) ProcessAnnotatedType<X> pat) {
        pat.setAnnotatedType(new AnnotatedTypeDecorator<>(pat.getAnnotatedType(), METRICS_BINDING));
    }

    private void metricProducerField(@Observes ProcessProducerField<? extends Metric, ?> ppf) {
        metrics.put(ppf.getBean(), ppf.getAnnotatedProducerField());
    }

    private void metricProducerMethod(@Observes ProcessProducerMethod<? extends Metric, ?> ppm) {
        // Skip the Metrics CDI alternatives
        if (!ppm.getBean().getBeanClass().equals(MetricProducer.class))
            metrics.put(ppm.getBean(), ppm.getAnnotatedProducerMethod());
    }

    private void healthCheckProducerField(@Observes ProcessProducerField<? extends HealthCheck, ?> ppf) {
        healthChecks.put(ppf.getBean(), ppf.getAnnotatedProducerField());
    }

    private void healthCheckProducerMethod(@Observes ProcessProducerMethod<? extends HealthCheck, ?> ppm) {
        healthChecks.put(ppm.getBean(), ppm.getAnnotatedProducerMethod());
    }

    private void defaultMetricRegistry(@Observes AfterBeanDiscovery abd, BeanManager manager) {
        if (manager.getBeans(MetricRegistry.class).isEmpty())
            abd.addBean(new SyntheticBean<MetricRegistry>(manager, MetricRegistry.class, "metric-registry", "Default Metric Registry Bean"));

        if (manager.getBeans(HealthCheckRegistry.class).isEmpty())
            abd.addBean(new SyntheticBean<HealthCheckRegistry>(manager, HealthCheckRegistry.class, "health-check-registry", "Default Health Check Registry Bean"));
    }

    private void configuration(@Observes AfterDeploymentValidation adv, BeanManager manager) {
        // Fire configuration event
        manager.fireEvent(configuration);
        configuration.unmodifiable();

        // Produce and register custom metrics
        MetricRegistry registry = getReference(manager, MetricRegistry.class);
        MetricName metricName = getReference(manager, MetricName.class);
        for (Map.Entry<Bean<?>, AnnotatedMember<?>> bean : metrics.entrySet()) {
            // TODO: add MetricSet metrics into the metric registry
            if (bean.getKey().getTypes().contains(MetricSet.class)
                // skip non @Default beans
                || !bean.getKey().getQualifiers().contains(DEFAULT)
                // skip producer methods with injection point
                || hasInjectionPoints(bean.getValue()))
                continue;
            registry.register(metricName.of(bean.getValue()), (Metric) getReference(manager, bean.getValue().getBaseType(), bean.getKey()));
        }

        // Let's clear the collected metric producers
        metrics.clear();

        // Register detected HealthChecks.
        HealthCheckRegistry healthCheckRegistry = getReference(manager, HealthCheckRegistry.class);

        // Produced Beans.
        for (Map.Entry<Bean<?>, AnnotatedMember<?>> bean : healthChecks.entrySet()) {
            // skip producer methods with injection points.
            if (hasInjectionPoints(bean.getValue()))
                continue;

            String name = bean.getKey().getName();
            if (name == null) {
                name = bean.getKey().getBeanClass().getName() + "." + bean.getValue().getJavaMember().getName();
            }
            healthCheckRegistry.register(name, (HealthCheck) getReference(manager, bean.getValue().getBaseType(), bean.getKey()));
        }

        // Declarative Scoped Beans
        for (Bean<?> bean : manager.getBeans(HealthCheck.class)) {
            if (healthChecks.containsKey(bean))
                continue;

            String name = bean.getName();
            if (name == null) {
                name = bean.getBeanClass().getName();
            }
            healthCheckRegistry.register(name, (HealthCheck) manager.getReference(bean, bean.getBeanClass(), manager.createCreationalContext(bean)));
        }
        // Clear out collected health check producers.
        healthChecks.clear();
    }

    private static <T extends Annotation> void declareAsInterceptorBinding(Class<T> annotation, BeanManager manager, BeforeBeanDiscovery bbd) {
        AnnotatedType<T> annotated = manager.createAnnotatedType(annotation);
        Set<AnnotatedMethod<? super T>> methods = new HashSet<>();
        for (AnnotatedMethod<? super T> method : annotated.getMethods())
            methods.add(new AnnotatedMethodDecorator<>(method, NON_BINDING));

        bbd.addInterceptorBinding(new AnnotatedTypeDecorator<>(annotated, INTERCEPTOR_BINDING, methods));
    }

    private static <T> T getReference(BeanManager manager, Class<T> type) {
        return getReference(manager, type, manager.resolve(manager.getBeans(type)));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getReference(BeanManager manager, Type type, Bean<?> bean) {
        return (T) manager.getReference(bean, type, manager.createCreationalContext(bean));
    }

    private static boolean hasInjectionPoints(AnnotatedMember<?> member) {
        if (!(member instanceof AnnotatedMethod))
            return false;
        AnnotatedMethod<?> method = (AnnotatedMethod<?>) member;
        for (AnnotatedParameter<?> parameter : method.getParameters()) {
            if (parameter.getBaseType().equals(InjectionPoint.class))
                return true;
        }
        return false;
    }
}