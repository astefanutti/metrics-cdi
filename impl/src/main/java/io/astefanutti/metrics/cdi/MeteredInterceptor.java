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
import com.codahale.metrics.annotation.Metered;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

@Metered
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
/* packaged-private */ class MeteredInterceptor {

    private final MetricRegistry registry;

    private final MetricResolver resolver;

    @Inject
    private MeteredInterceptor(MetricRegistry registry, MetricResolver resolver) {
        this.registry = registry;
        this.resolver = resolver;
    }

    @AroundConstruct
    private Object meteredConstructor(InvocationContext context) throws Exception {
        return meteredCallable(context, context.getConstructor());
    }

    @AroundInvoke
    private Object meteredMethod(InvocationContext context) throws Exception {
        return meteredCallable(context, context.getMethod());
    }

    private <E extends Member & AnnotatedElement> Object meteredCallable(InvocationContext context, E element) throws Exception {
        String name = resolver.metered(element).metricName();
        Meter meter = (Meter) registry.getMetrics().get(name);
        if (meter == null)
            throw new IllegalStateException("No meter with name [" + name + "] found in registry [" + registry + "]");

        meter.mark();
        return context.proceed();
    }
}
