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

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;

@Singleton
/* package-private */ final class MetricNameStrategyProducer {

    @Produces
    @Singleton
    private MetricNameStrategy metricNameStrategy(BeanManager manager) {
        try {
            // Cannot be inlined as OWB throws a NPE when manager.getELResolver() gets called
            ExpressionFactory factory = ExpressionFactory.newInstance();
            return new MetricNameElStrategy(manager.getELResolver(), manager.wrapExpressionFactory(factory));
        } catch (ELException cause) {
            // Falls back to SE
            return new MetricNameSeStrategy(manager);
        }
    }
}
