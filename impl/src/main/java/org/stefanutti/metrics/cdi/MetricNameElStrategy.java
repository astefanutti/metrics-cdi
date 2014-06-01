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

import javax.el.*;
import javax.enterprise.inject.Vetoed;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Vetoed
/* package-private */ final class MetricNameElStrategy implements MetricNameStrategy {

    private static final Pattern expression = Pattern.compile("[#|$]\\{(.*)\\}");

    private final ELResolver elResolver;

    private final ExpressionFactory expressionFactory;

    MetricNameElStrategy(ELResolver resolver, ExpressionFactory expressionFactory) {
        CompositeELResolver composite = new CompositeELResolver();
        composite.add(resolver);
        composite.add(new MapELResolver());
        composite.add(new ListELResolver());
        composite.add(new ArrayELResolver());
        composite.add(new ResourceBundleELResolver());
        composite.add(new BeanELResolver(true));
        this.elResolver = composite;
        this.expressionFactory = expressionFactory;
    }

    public String resolve(String name) {
        Matcher matcher = expression.matcher(name);
        if (matcher.matches())
            return evaluateElExpression(name);
        else
            return name;
    }

    private String evaluateElExpression(String name) {
        ELContext context = createELContext(elResolver, new FunctionMapper() {
            @Override
            public Method resolveFunction(String prefix, String localName) {
                return null;
            }
        }, new VariableMapper() {
            @Override
            public ValueExpression resolveVariable(String variable) {
                return null;
            }

            @Override
            public ValueExpression setVariable(String variable, ValueExpression expression) {
                return null;
            }
        });
        return (String) expressionFactory.createValueExpression(context, name, String.class).getValue(context);
    }

    private ELContext createELContext(final ELResolver resolver, final FunctionMapper functionMapper, final VariableMapper variableMapper) {

        return new ELContext() {
            @Override
            public ELResolver getELResolver() {
                return resolver;
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return functionMapper;
            }

            @Override
            public VariableMapper getVariableMapper() {
                return variableMapper;
            }
        };
    }
}
