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

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class MetricsExtension implements Extension {

    private <T> void processAnnotatedType(@Observes @WithAnnotations({ExceptionMetered.class, Gauge.class, Metered.class, Timed.class}) ProcessAnnotatedType<T> event) {
        boolean gauge = false;
        Set<AnnotatedMethod<? super T>> decoratedMethods = new HashSet<AnnotatedMethod<? super T>>(4);
        for (AnnotatedMethod<? super T> method : event.getAnnotatedType().getMethods()) {
            if (method.isAnnotationPresent(ExceptionMetered.class))
                decoratedMethods.add(getAnnotatedMethodDecorator(method, ExceptionMeteredBindingLiteral.INSTANCE));
            if (method.isAnnotationPresent(Metered.class))
                decoratedMethods.add(getAnnotatedMethodDecorator(method, MeteredBindingLiteral.INSTANCE));
            if (method.isAnnotationPresent(Timed.class))
                decoratedMethods.add(getAnnotatedMethodDecorator(method, TimedBindingLiteral.INSTANCE));
            if (method.isAnnotationPresent(Gauge.class))
                gauge = true;
        }
        AnnotatedType<T> annotatedType = new AnnotatedTypeDecorator<T>(event.getAnnotatedType(), MetricsBindingLiteral.INSTANCE);
        // FIXME: removed when OWB will be CDI 1.1 compliant
        if (gauge /*decoratedMethods.isEmpty()*/)
            event.setAnnotatedType(annotatedType);
        else if (!decoratedMethods.isEmpty())
            event.setAnnotatedType(new AnnotatedTypeMethodDecorator<T>(annotatedType, decoratedMethods));
    }

    private static <X> AnnotatedMethod<X> getAnnotatedMethodDecorator(AnnotatedMethod<X> annotatedMethod, Annotation annotated) {
        return new AnnotatedMethodDecorator<X>(annotatedMethod, annotated);
    }
}