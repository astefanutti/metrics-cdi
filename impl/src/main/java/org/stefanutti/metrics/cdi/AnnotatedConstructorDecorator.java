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
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

/* packaged-private */ final class AnnotatedConstructorDecorator<X> extends AnnotatedDecorator implements AnnotatedConstructor<X> {

    private final AnnotatedConstructor<X> decoratedConstructor;

    AnnotatedConstructorDecorator(AnnotatedConstructor<X> decoratedConstructor, Set<Annotation> decoratingAnnotations) {
        super(decoratedConstructor, decoratingAnnotations);
        this.decoratedConstructor = decoratedConstructor;
    }

    @Override
    public Constructor<X> getJavaMember() {
        return decoratedConstructor.getJavaMember();
    }

    @Override
    public boolean isStatic() {
        return decoratedConstructor.isStatic();
    }

    @Override
    public AnnotatedType<X> getDeclaringType() {
        return decoratedConstructor.getDeclaringType();
    }

    @Override
    public List<AnnotatedParameter<X>> getParameters() {
        return decoratedConstructor.getParameters();
    }

    // TODO: factorize in parent class when figured out why OWB tests fail for AnnotatedTypeDecorator
    @Override
    public String toString() {
        return decoratedConstructor.toString();
    }

    @Override
    public int hashCode() {
        return decoratedConstructor.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return decoratedConstructor.equals(object);
    }
}
