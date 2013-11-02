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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@MeteredBinding
class MeteredInterceptor {

    @Inject
    private MetricRegistry registry;

    @AroundInvoke
    private Object meteredMethod(InvocationContext context) throws Exception {
        Metered metered = context.getMethod().getAnnotation(Metered.class);
        String finalName = metered.name().isEmpty() ? context.getMethod().getName() : metered.name();
        Meter meter = registry.meter(metered.absolute() ? finalName : MetricRegistry.name(context.getMethod().getDeclaringClass(), finalName));
        meter.mark();
        return context.proceed();
    }
}
