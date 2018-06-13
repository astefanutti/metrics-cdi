/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.astefanutti.metrics.cdi;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;

import javax.annotation.Priority;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Executable;
import javax.interceptor.AroundTimeout;

@Timed
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE  + 10)
/* package-private */ class TimedInterceptor {

    private final Bean<?> bean;

    private final MetricRegistry registry;

    private final MetricResolver resolver;

    @Inject
    private TimedInterceptor(@Intercepted Bean<?> bean, MetricRegistry registry, MetricResolver resolver) {
        this.bean = bean;
        this.registry = registry;
        this.resolver = resolver;
    }

    @AroundConstruct
    private Object timedConstructor(InvocationContext context) throws Exception {
        return timedCallable(context, context.getConstructor());
    }

    @AroundInvoke
    private Object timedMethod(InvocationContext context) throws Exception {
        return timedCallable(context, context.getMethod());
    }

    @AroundTimeout
    private Object timedTimeout(InvocationContext context) throws Exception {
        return timedCallable(context, context.getMethod());
    }

    private Object timedCallable(InvocationContext context, Executable executable) throws Exception {
        String name = resolver.timed(bean.getBeanClass(), executable).metricName();
        Timer timer = (Timer) registry.getMetrics().get(name);
        if (timer == null)
            throw new IllegalStateException("No timer with name [" + name + "] found in registry [" + registry + "]");

        Timer.Context time = timer.time();
        try {
            return context.proceed();
        } finally {
            time.stop();
        }
    }
}
