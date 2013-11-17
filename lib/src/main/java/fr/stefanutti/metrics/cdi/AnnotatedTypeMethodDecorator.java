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

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class AnnotatedTypeMethodDecorator<X> implements AnnotatedType<X> {

    private final AnnotatedType<X> decoratedType;

    private final Set<AnnotatedMethod<? super X>> decoratedMethods;

    AnnotatedTypeMethodDecorator(AnnotatedType<X> decoratedType, Set<AnnotatedMethod<? super X>> decoratedMethods) {
        this.decoratedType = decoratedType;
        this.decoratedMethods = decoratedMethods;
    }

    @Override
    public Class<X> getJavaClass() {
        return decoratedType.getJavaClass();
    }

    @Override
    public Set<AnnotatedConstructor<X>> getConstructors() {
        return decoratedType.getConstructors();
    }

    @Override
    public Set<AnnotatedMethod<? super X>> getMethods() {
        Set<AnnotatedMethod<? super X>> methods = new HashSet<AnnotatedMethod<? super X>>(decoratedType.getMethods());
        for (AnnotatedMethod<? super X> method : decoratedMethods) {
            methods.remove(method);
            methods.add(method);
        }
        return Collections.unmodifiableSet(methods);
    }

    @Override
    public Set<AnnotatedField<? super X>> getFields() {
        return decoratedType.getFields();
    }

    @Override
    public Type getBaseType() {
        return decoratedType.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure() {
        return decoratedType.getTypeClosure();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return decoratedType.getAnnotation(annotationType);
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return decoratedType.getAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return decoratedType.isAnnotationPresent(annotationType);
    }
}
