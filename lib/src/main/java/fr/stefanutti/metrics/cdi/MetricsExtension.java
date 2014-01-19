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
package fr.stefanutti.metrics.cdi;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class MetricsExtension implements Extension {

    private boolean hasMetricRegistry;

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
        // FIXME: removed when OWB will be CDI 1.1 compliant
        if (gauge /*decoratedMethods.isEmpty()*/)
            pat.setAnnotatedType(annotatedType);
        else if (!decoratedMethods.isEmpty())
            pat.setAnnotatedType(new AnnotatedTypeMethodDecorator<X>(annotatedType, decoratedMethods));
    }

    private <X> void processBean(@Observes ProcessBean<X> pb) {
        if (pb.getBean().getTypes().contains(MetricRegistry.class))
            hasMetricRegistry = true;
    }

    private void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager manager) {
        if (!hasMetricRegistry)
            abd.addBean(new MetricRegistryBean(manager));
    }

    private static <X> AnnotatedMethod<X> getAnnotatedMethodDecorator(AnnotatedMethod<X> annotatedMethod, Annotation annotated) {
        return new AnnotatedMethodDecorator<X>(annotatedMethod, annotated);
    }
}