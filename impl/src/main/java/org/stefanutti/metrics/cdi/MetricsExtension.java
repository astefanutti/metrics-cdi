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

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.CachedGauge;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.inject.spi.WithAnnotations;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MetricsExtension implements Extension {

    private final Map<Bean<?>, AnnotatedMember<?>> metrics = new HashMap<>();

    private <X> void processMetricsAnnotatedType(@Observes @WithAnnotations({CachedGauge.class, Counted.class, ExceptionMetered.class, Gauge.class, Metered.class, Timed.class}) ProcessAnnotatedType<X> pat) {
        Set<AnnotatedConstructor<X>> decoratedConstructors = new HashSet<>();
        for (AnnotatedConstructor<X> constructor : pat.getAnnotatedType().getConstructors()) {
            Set<Annotation> annotations = new HashSet<>();
            if (constructor.isAnnotationPresent(Counted.class))
                annotations.add(CountedBindingLiteral.INSTANCE);
            if (constructor.isAnnotationPresent(ExceptionMetered.class))
                annotations.add(ExceptionMeteredBindingLiteral.INSTANCE);
            if (constructor.isAnnotationPresent(Metered.class))
                annotations.add(MeteredBindingLiteral.INSTANCE);
            if (constructor.isAnnotationPresent(Timed.class))
                annotations.add(TimedBindingLiteral.INSTANCE);

            if (!annotations.isEmpty())
                decoratedConstructors.add(new AnnotatedConstructorDecorator<>(constructor, annotations));
        }

        boolean gauge = false;
        Set<AnnotatedMethod<? super X>> decoratedMethods = new HashSet<>();
        for (AnnotatedMethod<? super X> method : pat.getAnnotatedType().getMethods()) {
            Set<Annotation> annotations = new HashSet<>();
            if (shouldHaveMetricBinding(method, Counted.class))
                annotations.add(CountedBindingLiteral.INSTANCE);
            if (shouldHaveMetricBinding(method, ExceptionMetered.class))
                annotations.add(ExceptionMeteredBindingLiteral.INSTANCE);
            if (shouldHaveMetricBinding(method, Metered.class))
                annotations.add(MeteredBindingLiteral.INSTANCE);
            if (shouldHaveMetricBinding(method, Timed.class))
                annotations.add(TimedBindingLiteral.INSTANCE);
            if (method.isAnnotationPresent(CachedGauge.class) || method.isAnnotationPresent(Gauge.class))
                gauge = true;

            if (!annotations.isEmpty())
                decoratedMethods.add(new AnnotatedMethodDecorator<>(method, annotations));
        }

        // FIXME: remove when OWB supports @WithAnnotations, see OWB-980
        if (gauge || !decoratedConstructors.isEmpty() || !decoratedMethods.isEmpty())
            pat.setAnnotatedType(new AnnotatedTypeDecorator<>(pat.getAnnotatedType(), MetricsBindingLiteral.INSTANCE, decoratedConstructors, decoratedMethods));
    }

    private boolean shouldHaveMetricBinding(AnnotatedMethod<?> method, Class<? extends Annotation> type) {
        return method.isAnnotationPresent(type) || Modifier.isPublic(method.getJavaMember().getModifiers()) && method.getDeclaringType().isAnnotationPresent(type);
    }

    private void processMetricProducerField(@Observes ProcessProducerField<? extends Metric, ?> ppf) {
        metrics.put(ppf.getBean(), ppf.getAnnotatedProducerField());
    }

    private void processMetricProducerMethod(@Observes ProcessProducerMethod<? extends Metric, ?> ppm) {
        // Skip the Metrics CDI alternatives
        if (!ppm.getBean().getBeanClass().equals(MetricProducer.class))
            metrics.put(ppm.getBean(), ppm.getAnnotatedProducerMethod());
    }

    private void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager manager) {
        if (manager.getBeans(MetricRegistry.class, AnyLiteral.INSTANCE, DefaultLiteral.INSTANCE).isEmpty())
            abd.addBean(new MetricRegistryBean(manager));
    }

    private void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager manager) {
        MetricProducer producer = getBeanInstance(manager, MetricProducer.class);

        for (Map.Entry<Bean<?>, AnnotatedMember<?>> metric : metrics.entrySet())
            producer.produceMetric(manager, metric.getKey(), metric.getValue());

        // Let's clear the collected metric producers
        metrics.clear();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getBeanInstance(BeanManager manager, Class<T> clazz) {
        Bean<?> bean = manager.resolve(manager.getBeans(clazz, AnyLiteral.INSTANCE));
        return (T) manager.getReference(bean, clazz, manager.createCreationalContext(null));
    }
}