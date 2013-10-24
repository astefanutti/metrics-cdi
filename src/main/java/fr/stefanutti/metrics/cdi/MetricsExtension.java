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
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

class MetricsExtension implements Extension {

    private <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {
        for (AnnotatedMethod method : event.getAnnotatedType().getMethods()) {
            if (method.isAnnotationPresent(ExceptionMetered.class)) {
                AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>()
                    .readFromType(event.getAnnotatedType())
                    .addToClass(AnnotationInstanceProvider.of(MetricsBinding.class))
                    .addToMethod(method, AnnotationInstanceProvider.of(ExceptionMeteredBinding.class));
                event.setAnnotatedType(builder.create());
            }

            if (method.isAnnotationPresent(Metered.class)) {
                AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>()
                    .readFromType(event.getAnnotatedType())
                    .addToClass(AnnotationInstanceProvider.of(MetricsBinding.class))
                    .addToMethod(method, AnnotationInstanceProvider.of(MeteredBinding.class));
                event.setAnnotatedType(builder.create());
            }

            if (method.isAnnotationPresent(Timed.class)) {
                AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>()
                    .readFromType(event.getAnnotatedType())
                    .addToClass(AnnotationInstanceProvider.of(MetricsBinding.class))
                    .addToMethod(method, AnnotationInstanceProvider.of(TimedBinding.class));
                event.setAnnotatedType(builder.create());
            }
        }
    }
}
