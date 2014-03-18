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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@ExceptionMeteredBinding
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
/* packaged-private */ class ExceptionMeteredInterceptor {

    @Inject
    private MetricRegistry registry;

    @AroundInvoke
    private Object exceptionMeteredMethod(InvocationContext context) throws Throwable {
        ExceptionMetered metered = context.getMethod().getAnnotation(ExceptionMetered.class);
        String name = metered.name().isEmpty() ? context.getMethod().getName() + "." + ExceptionMetered.DEFAULT_NAME_SUFFIX : metered.name();
        String finalName = metered.absolute() ? name : MetricRegistry.name(context.getMethod().getDeclaringClass(), name);
        Meter meter = (Meter) registry.getMetrics().get(finalName);
        if (meter == null)
            throw new IllegalStateException("No meter with name [" + finalName + "] found in registry [" + registry + "]");

        try {
            return context.proceed();
        } catch (Throwable throwable) {
            if (metered.cause().isInstance(throwable))
                meter.mark();
            throw throwable;
        }
    }
}
