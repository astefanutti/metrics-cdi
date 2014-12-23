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
package io.astefanutti.metrics.cdi;

import com.codahale.metrics.MetricRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* packaged-private */ final class MetricRegistryBean implements Bean<MetricRegistry>, PassivationCapable {

    private final Set<Annotation> qualifiers = new HashSet<>(Arrays.<Annotation>asList(new AnnotationLiteral<Any>(){}, new AnnotationLiteral<Default>(){}));

    private final Set<Type> types;

    private final InjectionTarget<MetricRegistry> target;

    MetricRegistryBean(BeanManager manager) {
        AnnotatedType<MetricRegistry> annotatedType = manager.createAnnotatedType(MetricRegistry.class);
        this.types = annotatedType.getTypeClosure();
        this.target = manager.createInjectionTarget(annotatedType);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.unmodifiableSet(qualifiers);
    }

    @Override
    public MetricRegistry create(CreationalContext<MetricRegistry> context) {
        MetricRegistry registry = target.produce(context);
        target.inject(registry, context);
        target.postConstruct(registry);
        context.push(registry);
        return registry;
    }

    @Override
    public void destroy(MetricRegistry instance, CreationalContext<MetricRegistry> context) {
        target.preDestroy(instance);
        target.dispose(instance);
        context.release();
    }

    @Override
    public Class<MetricRegistry> getBeanClass() {
        return MetricRegistry.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return "metric-registry";
    }

    @Override
    public String toString() {
        return "Default Metric Registry Bean";
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public Set<Type> getTypes() {
        return Collections.unmodifiableSet(types);
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }
}
