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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

@Interceptor
@ExceptionMetered
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
/* package-private */ class ExceptionMeteredInterceptor {

    private final MetricRegistry registry;

    private final MetricResolver resolver;

    @Inject
    private ExceptionMeteredInterceptor(MetricRegistry registry, MetricResolver resolver) {
        this.registry = registry;
        this.resolver = resolver;
    }

    @AroundConstruct
    private Object meteredConstructor(InvocationContext context) throws Throwable {
        return meteredCallable(context, context.getConstructor());
    }

    @AroundInvoke
    private Object meteredMethod(InvocationContext context) throws Throwable {
        return meteredCallable(context, context.getMethod());
    }

    private <E extends Member & AnnotatedElement> Object meteredCallable(InvocationContext context, E element) throws Throwable {
        MetricResolver.Of<ExceptionMetered> exceptionMetered = resolver.exceptionMetered(element);
        Meter meter = (Meter) registry.getMetrics().get(exceptionMetered.metricName());
        if (meter == null)
            throw new IllegalStateException("No meter with name [" + exceptionMetered.metricName() + "] found in registry [" + registry + "]");

        try {
            return context.proceed();
        } catch (Throwable throwable) {
            if (exceptionMetered.metricAnnotation().cause().isInstance(throwable))
                meter.mark();

            throw throwable;
        }
    }
}
