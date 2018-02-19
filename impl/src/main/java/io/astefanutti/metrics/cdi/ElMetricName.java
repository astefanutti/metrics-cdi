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

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.enterprise.inject.Vetoed;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Vetoed
/* package-private */ final class ElMetricName extends SeMetricName {

    private static final Pattern PATTERN = Pattern.compile("[#|$]\\{(.*)\\}");

    private final ELResolver elResolver;

    private final ExpressionFactory expressionFactory;

    ElMetricName(ELResolver resolver, ExpressionFactory expressionFactory, MetricsExtension extension) {
        super(extension);
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

    @Override
    public String of(String attribute) {
        Matcher matcher = PATTERN.matcher(attribute);
        // Avoid creating objects if no expressions are found
        if (!matcher.find())
            return super.of(attribute);
        else
            return evaluateCompositeExpression(matcher);
    }

    private String evaluateCompositeExpression(Matcher matcher) {
        StringBuffer buffer = new StringBuffer();
        do {
            String result = evaluateExpression(matcher.group());
            matcher.appendReplacement(buffer, result != null ? result : "");
        } while (matcher.find());

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String evaluateExpression(String name) {
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
