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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import fr.stefanutti.metrics.cdi.bean.CounterFieldBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class CounterFieldTest {

    private final static String COUNTER_NAME = MetricRegistry.name(CounterFieldBean.class, "counterName");


    @Deployment
    private static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(CounterFieldBean.class)
            // Metrics CDI extension
            .addPackages(false, Filters.exclude(".*Test.*"), MetricsExtension.class.getPackage())
            .addAsServiceProvider(Extension.class, MetricsExtension.class)
            // Bean archive deployment descriptor
            .addAsManifestResource("META-INF/beans.xml");
    }

    @Produces
    @Singleton
    private static MetricRegistry registry = new MetricRegistry();

    @Inject
    private CounterFieldBean bean;

    @Test
    @InSequence(1)
    public void counterFieldRegistered() {
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(COUNTER_NAME));
    }

    @Test
    @InSequence(2)
    public void incrementCounterField() {
        assertThat("Counter is not registered correctly", registry.getCounters(), hasKey(COUNTER_NAME));
        Counter counter = registry.getCounters().get(COUNTER_NAME);

        // Call the increment method and assert the counter is up-to-date
        long value = Math.round(Math.random() * Long.MAX_VALUE);
        bean.increment(value);
        assertThat("Counter value is incorrect", counter.getCount(), is(equalTo(value)));
    }
}