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
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

@Interceptor
@MetricsBinding
class MetricsInterceptor {

    private final Class<?> bean;

    private final MetricRegistry registry;

    @Inject
    private MetricsInterceptor(BeanManager manager, MetricRegistry registry, InjectionPoint point) {
        this.bean = manager.resolve(manager.getBeans(point.getType())).getBeanClass();
        this.registry = registry;
    }

    @PostConstruct
    private void metrics(InvocationContext context) throws Exception {
        for (Method method : bean.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ExceptionMetered.class)) {
                ExceptionMetered metered = method.getAnnotation(ExceptionMetered.class);
                String finalName = metered.name().isEmpty() ? method.getName() + "." + ExceptionMetered.DEFAULT_NAME_SUFFIX : metered.name();
                registry.meter(metered.absolute() ? finalName : MetricRegistry.name(bean, finalName));
            }
            if (method.isAnnotationPresent(Metered.class)) {
                Metered metered = method.getAnnotation(Metered.class);
                String finalName = metered.name().isEmpty() ? method.getName() : metered.name();
                registry.meter(metered.absolute() ? finalName : MetricRegistry.name(bean, finalName));
            }
            if (method.isAnnotationPresent(Timed.class)) {
                Timed timed = method.getAnnotation(Timed.class);
                String finalName = timed.name().isEmpty() ? method.getName() : timed.name();
                registry.timer(timed.absolute() ? finalName : MetricRegistry.name(bean, finalName));
            }
        }
        context.proceed();
    }
}
