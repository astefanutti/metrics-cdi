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
package fr.stefanutti.metrics.cdi.se;

import com.codahale.metrics.MetricRegistry;
import fr.stefanutti.metrics.cdi.MetricsExtension;
import fr.stefanutti.metrics.cdi.se.util.MetricsUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class DefaultNameMetricMethodTest {

    private final static String[] METRIC_NAMES = {"defaultNameTimedMethod", "defaultNameMeteredMethod", "defaultNameExceptionMeteredMethod.exceptions"};

    private final static String[] ABSOLUTE_METRIC_NAMES = {"absoluteDefaultNameTimedMethod", "absoluteDefaultNameMeteredMethod", "absoluteDefaultNameExceptionMeteredMethod.exceptions"};

    private Set<String> metricNames() {
        Set<String> names = MetricsUtil.absoluteMetricNameSet(DefaultNameMetricMethodBean.class, METRIC_NAMES);
        names.addAll(Arrays.asList(ABSOLUTE_METRIC_NAMES));
        return names;
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(DefaultNameMetricMethodBean.class)
            // Metrics CDI extension
            .addPackages(false, MetricsExtension.class.getPackage())
            //.addAsServiceProvider(Extension.class, MetricsExtension.class)
            // Bean archive deployment descriptor
            .addAsManifestResource("beans.xml");
    }

    @Produces
    @Singleton
    private static MetricRegistry registry = new MetricRegistry();

    @Inject
    private DefaultNameMetricMethodBean bean;

    @Test
    public void metricMethodsWithDefaultNamingConvention() {
        assertThat("Metrics are not registered correctly", registry.getMetrics().keySet(), is(equalTo(metricNames())));
    }
}