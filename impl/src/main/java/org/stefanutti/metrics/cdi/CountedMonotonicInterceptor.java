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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@CountedBinding(monotonic = true)
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
/* packaged-private */ class CountedMonotonicInterceptor {

    private final MetricRegistry registry;

    private final MetricNameHelper nameHelper;

    @Inject
    private CountedMonotonicInterceptor(MetricRegistry registry, MetricNameHelper nameHelper) {
        this.registry = registry;
        this.nameHelper = nameHelper;
    }

    @AroundInvoke
    private Object countedMethod(InvocationContext context) throws Exception {
        String name = nameHelper.counterName(context.getMethod());
        Counter counter = (Counter) registry.getMetrics().get(name);
        if (counter == null)
            throw new IllegalStateException("No counter with name [" + name + "] found in registry [" + registry + "]");

        counter.inc();
        return context.proceed();
    }
}
