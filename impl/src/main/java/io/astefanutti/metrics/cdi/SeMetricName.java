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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metric;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static io.astefanutti.metrics.cdi.MetricsParameter.UseAbsoluteName;

@Vetoed
/* package-private */ class SeMetricName implements MetricName {

    private final MetricsExtension extension;

    SeMetricName(MetricsExtension extension) {
        this.extension = extension;
    }

    @Override
    public String of(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        if (annotated instanceof AnnotatedMember)
            return of((AnnotatedMember<?>) annotated);
        else if (annotated instanceof AnnotatedParameter)
            return of((AnnotatedParameter<?>) annotated);
        else
            throw new IllegalArgumentException("Unable to retrieve metric name for injection point [" + ip + "], only members and parameters are supported");
    }

    @Override
    public String of(AnnotatedMember<?> member) {
        if (member.isAnnotationPresent(Metric.class)) {
            Metric metric = member.getAnnotation(Metric.class);
            String name = (metric.name().isEmpty()) ? member.getJavaMember().getName() : of(metric.name());
            return metric.absolute() | extension.getParameter(UseAbsoluteName, Boolean.class).orElse(false) ? name : MetricRegistry.name(member.getJavaMember().getDeclaringClass(), name);
        } else {
            return extension.getParameter(UseAbsoluteName, Boolean.class).orElse(false) ? member.getJavaMember().getName() : MetricRegistry.name(member.getJavaMember().getDeclaringClass(), member.getJavaMember().getName());
        }
    }

    @Override
    public String of(String attribute) {
        return attribute;
    }

    private String of(AnnotatedParameter<?> parameter) {
        if (parameter.isAnnotationPresent(Metric.class)) {
            Metric metric = parameter.getAnnotation(Metric.class);
            String name = (metric.name().isEmpty()) ? getParameterName(parameter) : of(metric.name());
            return metric.absolute() | extension.getParameter(UseAbsoluteName, Boolean.class).orElse(false) ? name : MetricRegistry.name(parameter.getDeclaringCallable().getJavaMember().getDeclaringClass(), name);
        } else {
            return extension.getParameter(UseAbsoluteName, Boolean.class).orElse(false) ? getParameterName(parameter) : MetricRegistry.name(parameter.getDeclaringCallable().getJavaMember().getDeclaringClass(), getParameterName(parameter));
        }
    }

    // To be refactored eventually when CDI SPI integrates JEP-118.
    // TODO: move into a separate metric name strategy
    private String getParameterName(AnnotatedParameter<?> parameter) {
        Parameter[] parameters = ((Method) parameter.getDeclaringCallable().getJavaMember()).getParameters();
        Parameter param = parameters[parameter.getPosition()];
        if (param.isNamePresent()) {
            return param.getName();
        }
        else {
            throw new UnsupportedOperationException("Unable to retrieve name for parameter [" + parameter + "], activate the -parameters compiler argument or annotate the injected parameter with the @Metric annotation");
        }
    }
}
