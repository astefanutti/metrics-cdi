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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

import static io.astefanutti.metrics.cdi.CdiHelper.getReference;
import static io.astefanutti.metrics.cdi.CdiHelper.hasInjectionPoints;

public class HealthCheckExtension implements Extension {

    private final Map<Bean<?>, AnnotatedMember<?>> healthChecks = new HashMap<>();

    private void healthCheckProducerField(@Observes ProcessProducerField<? extends HealthCheck, ?> ppf) {
        healthChecks.put(ppf.getBean(), ppf.getAnnotatedProducerField());
    }

    private void healthCheckProducerMethod(@Observes ProcessProducerMethod<? extends HealthCheck, ?> ppm) {
        healthChecks.put(ppm.getBean(), ppm.getAnnotatedProducerMethod());
    }

    private void defaultHealthCheckRegistry(@Observes AfterBeanDiscovery abd, BeanManager manager) {
        if (manager.getBeans(HealthCheckRegistry.class).isEmpty())
            abd.addBean(new SyntheticBean<HealthCheckRegistry>(manager, HealthCheckRegistry.class, "health-check-registry", "Default Health Check Registry Bean"));
    }

    private void configuration(@Observes AfterDeploymentValidation adv, BeanManager manager) {
        // Register detected HealthChecks
        HealthCheckRegistry healthCheckRegistry = getReference(manager, HealthCheckRegistry.class);

        // Produced Beans.
        for (Map.Entry<Bean<?>, AnnotatedMember<?>> bean : healthChecks.entrySet()) {
            // skip producer methods with injection points.
            if (hasInjectionPoints(bean.getValue()))
                continue;

            String name = bean.getKey().getName();
            if (name == null) {
                name = bean.getKey().getBeanClass().getName() + "." + bean.getValue().getJavaMember().getName();
            }
            healthCheckRegistry.register(name, (HealthCheck) getReference(manager, bean.getValue().getBaseType(), bean.getKey()));
        }

        // Declarative Scoped Beans
        for (Bean<?> bean : manager.getBeans(HealthCheck.class)) {
            if (healthChecks.containsKey(bean))
                continue;

            String name = bean.getName();
            if (name == null) {
                name = bean.getBeanClass().getName();
            }
            healthCheckRegistry.register(name, (HealthCheck) manager.getReference(bean, bean.getBeanClass(), manager.createCreationalContext(bean)));
        }
        // Clear out collected health check producers
        healthChecks.clear();
    }
}