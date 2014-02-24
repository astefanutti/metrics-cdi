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
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@TimedBinding
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
/* packaged-protected */ class TimedInterceptor {

    @Inject
    private MetricRegistry registry;

    @AroundInvoke
    private Object timedMethod(InvocationContext context) throws Exception {
        Timed timed = context.getMethod().getAnnotation(Timed.class);
        String name = timed.name().isEmpty() ? context.getMethod().getName() : timed.name();
        String finalName = timed.absolute() ? name : MetricRegistry.name(context.getMethod().getDeclaringClass(), name);
        Timer timer = (Timer) registry.getMetrics().get(finalName);
        if (timer == null)
            throw new IllegalStateException("No timer with name [" + finalName + "] found in registry [" + registry + "]");

        Timer.Context time = timer.time();
        try {
            return context.proceed();
        } finally {
            time.stop();
        }
    }
}
