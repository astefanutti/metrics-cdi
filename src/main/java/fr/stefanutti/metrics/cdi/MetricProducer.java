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
import com.codahale.metrics.Timer;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;

@Singleton
class MetricProducer {

    @Produces
    private Timer produceTimer(MetricRegistry registry, InjectionPoint point) {
        return registry.timer(metricName(point));
    }

    @Produces
    private Meter produceMeter(MetricRegistry registry, InjectionPoint point) {
        return registry.meter(metricName(point));
    }

    private String metricName(InjectionPoint point) {
        if (point.getAnnotated().isAnnotationPresent(Metric.class)) {
            Metric metric = point.getAnnotated().getAnnotation(Metric.class);
            String name = metric.name().isEmpty() ? point.getMember().getName() : metric.name();
            return metric.absolute() ? name : MetricRegistry.name(point.getMember().getDeclaringClass(), name);
        } else {
            return MetricRegistry.name(point.getMember().getDeclaringClass(), point.getMember().getName());
        }
    }
}
