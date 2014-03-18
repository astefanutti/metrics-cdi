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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Interceptor
@MetricsBinding
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
// See http://docs.oracle.com/javaee/7/tutorial/doc/interceptors.htm
/* packaged-private */ class MetricsInterceptor {

    private final MetricRegistry registry;

    @Inject
    private MetricsInterceptor(MetricRegistry registry) {
        this.registry = registry;
    }

    @AroundConstruct
    private Object metrics(InvocationContext context) throws Exception {
        Object target = context.proceed();

        Class<?> bean = context.getConstructor().getDeclaringClass();
        for (Method method : bean.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ExceptionMetered.class)) {
                ExceptionMetered metered = method.getAnnotation(ExceptionMetered.class);
                String name = metered.name().isEmpty() ? method.getName() + "." + ExceptionMetered.DEFAULT_NAME_SUFFIX : metered.name();
                registry.meter(metered.absolute() ? name : MetricRegistry.name(bean, name));
            }
            if (method.isAnnotationPresent(Gauge.class)) {
                Gauge gauge = method.getAnnotation(Gauge.class);
                String name = gauge.name().isEmpty() ? method.getName() : gauge.name();
                // FIXME: remove that discrepancy to support OWB 2.0.0
                registry.register(gauge.absolute() ? name : MetricRegistry.name(bean, name), new ForwardingGauge(method, context.getTarget() == null ? target : context.getTarget()));
            }
            if (method.isAnnotationPresent(Metered.class)) {
                Metered metered = method.getAnnotation(Metered.class);
                String name = metered.name().isEmpty() ? method.getName() : metered.name();
                registry.meter(metered.absolute() ? name : MetricRegistry.name(bean, name));
            }
            if (method.isAnnotationPresent(Timed.class)) {
                Timed timed = method.getAnnotation(Timed.class);
                String name = timed.name().isEmpty() ? method.getName() : timed.name();
                registry.timer(timed.absolute() ? name : MetricRegistry.name(bean, name));
            }
        }

        return target;
    }

    private static class ForwardingGauge implements com.codahale.metrics.Gauge<Object> {

        final Method method;

        final Object object;

        private ForwardingGauge(Method method, Object object) {
            this.method = method;
            this.object = object;
            method.setAccessible(true);
        }

        @Override
        public Object getValue() {
            return invokeMethod(method, object);
        }
    }

    private static Object invokeMethod(Method method, Object object) {
        try {
            return method.invoke(object);
        } catch (IllegalAccessException cause) {
            throw new IllegalStateException("Error while calling method [" + method + "]", cause);
        } catch (InvocationTargetException cause) {
            throw new IllegalStateException("Error while calling method [" + method + "]", cause);
        }
    }
}
