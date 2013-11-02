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

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AnnotatedMethodDecorator<X> implements AnnotatedMethod<X> {

    private final AnnotatedMethod<X> decoratedMethod;

    private final Annotation decoratingAnnotation;

    AnnotatedMethodDecorator(AnnotatedMethod<X> decoratedMethod, Annotation decoratingAnnotation) {
        this.decoratedMethod = decoratedMethod;
        this.decoratingAnnotation = decoratingAnnotation;
    }

    @Override
    public Method getJavaMember() {
        return decoratedMethod.getJavaMember();
    }

    @Override
    public boolean isStatic() {
        return decoratedMethod.isStatic();
    }

    @Override
    public AnnotatedType<X> getDeclaringType() {
        return decoratedMethod.getDeclaringType();
    }

    @Override
    public List<AnnotatedParameter<X>> getParameters() {
        return decoratedMethod.getParameters();
    }

    @Override
    public Type getBaseType() {
        return decoratedMethod.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure() {
        return decoratedMethod.getTypeClosure();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (annotationType.isAssignableFrom(decoratingAnnotation.annotationType()))
            return (T) decoratingAnnotation;
        else
            return decoratedMethod.getAnnotation(annotationType);
    }

    @Override
    public Set<Annotation> getAnnotations() {
        Set<Annotation> annotations = new HashSet<Annotation>(decoratedMethod.getAnnotations());
        annotations.add(decoratingAnnotation);
        return Collections.unmodifiableSet(annotations);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return annotationType.equals(decoratingAnnotation.annotationType()) || decoratedMethod.isAnnotationPresent(annotationType);
    }
}
