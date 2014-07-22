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
import com.codahale.metrics.annotation.CachedGauge;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Interceptor
@MetricsBinding
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
// See http://docs.oracle.com/javaee/7/tutorial/doc/interceptors.htm
/* packaged-private */ class MetricsInterceptor {

    private final MetricRegistry registry;

    private final MetricResolver resolver;

    @Inject
    private MetricsInterceptor(MetricRegistry registry, MetricResolver resolver) {
        this.registry = registry;
        this.resolver = resolver;
    }

    @AroundConstruct
    private Object metrics(InvocationContext context) throws Exception {
        Class<?> bean = context.getConstructor().getDeclaringClass();

        for (Constructor<?> constructor : bean.getDeclaredConstructors())
            registerMetrics(constructor);

        for (Method method : bean.getDeclaredMethods())
            registerMetrics(method);

        Object target = context.proceed();

        for (Method method : bean.getDeclaredMethods()) {
            MetricResolver.Of<CachedGauge> cachedGauge = resolver.cachedGauge(method);
            if (cachedGauge.isPresent())
                registry.register(cachedGauge.metricName(), new CachingGauge(new ForwardingGauge(method, context.getTarget()), cachedGauge.metricAnnotation().timeout(), cachedGauge.metricAnnotation().timeoutUnit()));

            MetricResolver.Of<Gauge> gauge = resolver.gauge(method);
            if (gauge.isPresent())
                registry.register(gauge.metricName(), new ForwardingGauge(method, context.getTarget()));
        }

        return target;
    }

    private <E extends Member & AnnotatedElement> void registerMetrics(E element) {
        MetricResolver.Of<Counted> counted = resolver.counted(element);
        if (counted.isPresent())
            registry.counter(counted.metricName());

        MetricResolver.Of<ExceptionMetered> exceptionMetered = resolver.exceptionMetered(element);
        if (exceptionMetered.isPresent())
            registry.meter(exceptionMetered.metricName());

        MetricResolver.Of<Metered> metered = resolver.metered(element);
        if (metered.isPresent())
            registry.meter(metered.metricName());

        MetricResolver.Of<Timed> timed = resolver.timed(element);
        if (timed.isPresent())
            registry.timer(timed.metricName());
    }

    private static final class CachingGauge extends com.codahale.metrics.CachedGauge<Object> {

        private final com.codahale.metrics.Gauge<?> gauge;
        
        private CachingGauge(com.codahale.metrics.Gauge<?> gauge, long timeout, TimeUnit timeoutUnit) {
            super(timeout, timeoutUnit);
            this.gauge = gauge;
        }

        @Override
        protected Object loadValue() {
            return gauge.getValue();
        }
    }

    private static final class ForwardingGauge implements com.codahale.metrics.Gauge<Object> {

        private final Method method;

        private final Object object;

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
