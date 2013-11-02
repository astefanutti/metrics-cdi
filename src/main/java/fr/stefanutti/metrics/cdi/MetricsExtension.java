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

class MetricsExtension implements Extension {

    private <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {
        for (AnnotatedMethod method : event.getAnnotatedType().getMethods()) {
            if (method.isAnnotationPresent(ExceptionMetered.class)) {
                AnnotatedType<T> annotatedType = new AnnotatedTypeDecorator<T>(event.getAnnotatedType(), MetricsBindingLiteral.INSTANCE);
                AnnotatedMethod<T> annotatedMethod = new AnnotatedMethodDecorator<T>(method, ExceptionMeteredBindingLiteral.INSTANCE);
                event.setAnnotatedType(new AnnotatedTypeMethodDecorator<T>(annotatedType, annotatedMethod));
            }
            if (method.isAnnotationPresent(Gauge.class)) {
                event.setAnnotatedType(new AnnotatedTypeDecorator<T>(event.getAnnotatedType(), MetricsBindingLiteral.INSTANCE));
            }
            if (method.isAnnotationPresent(Metered.class)) {
                AnnotatedType<T> annotatedType = new AnnotatedTypeDecorator<T>(event.getAnnotatedType(), MetricsBindingLiteral.INSTANCE);
                AnnotatedMethod<T> annotatedMethod = new AnnotatedMethodDecorator<T>(method, MeteredBindingLiteral.INSTANCE);
                event.setAnnotatedType(new AnnotatedTypeMethodDecorator<T>(annotatedType, annotatedMethod));
            }
            if (method.isAnnotationPresent(Timed.class)) {
                AnnotatedType<T> annotatedType = new AnnotatedTypeDecorator<T>(event.getAnnotatedType(), MetricsBindingLiteral.INSTANCE);
                AnnotatedMethod<T> annotatedMethod = new AnnotatedMethodDecorator<T>(method, TimedBindingLiteral.INSTANCE);
                event.setAnnotatedType(new AnnotatedTypeMethodDecorator<T>(annotatedType, annotatedMethod));
            }
        }
    }
}
