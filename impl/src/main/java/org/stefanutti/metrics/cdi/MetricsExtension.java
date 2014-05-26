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

import com.codahale.metrics.*;
import com.codahale.metrics.Metric;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.*;

public class MetricsExtension implements Extension {

    private boolean hasMetricRegistry;

    private final Map<Bean<?>, AnnotatedMember<?>> metrics = new HashMap<Bean<?>, AnnotatedMember<?>>();

    // TODO: give meaningful method names
    private <X> void processAnnotatedType(@Observes @WithAnnotations({ExceptionMetered.class, Gauge.class, Metered.class, Timed.class}) ProcessAnnotatedType<X> pat) {
        boolean gauge = false;
        Set<AnnotatedMethod<? super X>> decoratedMethods = new HashSet<AnnotatedMethod<? super X>>(4);
        for (AnnotatedMethod<? super X> method : pat.getAnnotatedType().getMethods()) {
            if (method.isAnnotationPresent(ExceptionMetered.class))
                decoratedMethods.add(getAnnotatedMethodDecorator(method, ExceptionMeteredBindingLiteral.INSTANCE));
            if (method.isAnnotationPresent(Metered.class))
                decoratedMethods.add(getAnnotatedMethodDecorator(method, MeteredBindingLiteral.INSTANCE));
            if (method.isAnnotationPresent(Timed.class))
                decoratedMethods.add(getAnnotatedMethodDecorator(method, TimedBindingLiteral.INSTANCE));
            if (method.isAnnotationPresent(Gauge.class))
                gauge = true;
        }
        AnnotatedType<X> annotatedType = new AnnotatedTypeDecorator<X>(pat.getAnnotatedType(), MetricsBindingLiteral.INSTANCE);
        // FIXME: remove when OWB will be CDI 1.1 compliant
        if (gauge /*decoratedMethods.isEmpty()*/)
            pat.setAnnotatedType(annotatedType);
        else if (!decoratedMethods.isEmpty())
            pat.setAnnotatedType(new AnnotatedTypeMethodDecorator<X>(annotatedType, decoratedMethods));
    }

    // TODO: use typed observers
    private <X> void processBean(@Observes ProcessBean<X> pb) {
        if (pb.getBean().getTypes().contains(MetricRegistry.class))
            hasMetricRegistry = true;
    }

    private <X> void processProducerField(@Observes ProcessProducerField<? extends Metric, X> ppf) {
        metrics.put(ppf.getBean(), ppf.getAnnotatedProducerField());
    }

    private <X> void processProducerMethod(@Observes ProcessProducerMethod<? extends Metric, X> ppm) {
        // Skip the Metrics CDI alternatives
        if (!ppm.getBean().getBeanClass().equals(MetricProducer.class))
            metrics.put(ppm.getBean(), ppm.getAnnotatedProducerMethod());
    }

    private void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager manager) {
        if (!hasMetricRegistry)
            abd.addBean(new MetricRegistryBean(manager));
    }

    private void afterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager manager) {
        Bean<?> registryBean = manager.resolve(manager.getBeans(MetricRegistry.class, AnyLiteral.INSTANCE));
        MetricRegistry registry = (MetricRegistry) manager.getReference(registryBean, MetricRegistry.class, manager.createCreationalContext(null));

        for (Map.Entry<Bean<?>, AnnotatedMember<?>> metric : metrics.entrySet()) {
            Metric reference = (Metric) manager.getReference(metric.getKey(), metric.getValue().getBaseType(), manager.createCreationalContext(null));
            registry.register(MetricProducer.metricName(metric.getValue(), manager), reference);
        }

        // Let's clear the collected metric producers
        metrics.clear();
    }

    private static <X> AnnotatedMethod<X> getAnnotatedMethodDecorator(AnnotatedMethod<X> annotatedMethod, Annotation annotated) {
        return new AnnotatedMethodDecorator<X>(annotatedMethod, annotated);
    }
}