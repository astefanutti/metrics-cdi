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

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* packaged-private */ final class AnnotatedTypeDecorator<X> extends AnnotatedDecorator implements AnnotatedType<X> {

    private final AnnotatedType<X> decoratedType;

    private final Set<AnnotatedConstructor<X>> decoratedConstructors;

    private final Set<AnnotatedMethod<? super X>> decoratedMethods;

    AnnotatedTypeDecorator(AnnotatedType<X> decoratedType, Annotation decoratingAnnotation, Set<AnnotatedConstructor<X>> decoratedConstructors, Set<AnnotatedMethod<? super X>> decoratedMethods) {
        super(decoratedType, decoratingAnnotation);
        this.decoratedType = decoratedType;
        this.decoratedConstructors = decoratedConstructors;
        this.decoratedMethods = decoratedMethods;
    }

    @Override
    public Class<X> getJavaClass() {
        return decoratedType.getJavaClass();
    }

    @Override
    public Set<AnnotatedConstructor<X>> getConstructors() {
        Set<AnnotatedConstructor<X>> constructors = new HashSet<>(decoratedType.getConstructors());
        for (AnnotatedConstructor<X> constructor : decoratedConstructors) {
            constructors.remove(constructor);
            constructors.add(constructor);
        }

        return Collections.unmodifiableSet(constructors);
    }

    @Override
    public Set<AnnotatedMethod<? super X>> getMethods() {
        Set<AnnotatedMethod<? super X>> methods = new HashSet<>(decoratedType.getMethods());
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
}
