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
package io.astefanutti.metrics.cdi.se;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.astefanutti.metrics.cdi.MetricsExtension;

import javax.inject.Inject;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class CachedGaugeMethodBeanTest {

    private final static String GAUGE_NAME = MetricRegistry.name(CachedGaugeMethodBean.class, "cachedGaugeMethod");

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(CachedGaugeMethodBean.class)
            // Metrics CDI extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private CachedGaugeMethodBean bean;

    @Before
    public void instantiateApplicationScopedBean() {
        // Let's trigger the instantiation of the application scoped bean explicitly
        // as only a proxy gets injected otherwise
        bean.getGauge();
    }

    @Test
    @InSequence(1)
    public void gaugeCalledWithDefaultValue() {
        assertThat("Gauge is not registered correctly", registry.getGauges(), hasKey(GAUGE_NAME));
        @SuppressWarnings("unchecked")
        Gauge<Long> gauge = registry.getGauges().get(GAUGE_NAME);

        // Make sure that the gauge has the expected value
        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(0L)));
    }

    @Test
    @InSequence(2)
    public void callGaugeAfterSetterCall() throws InterruptedException {
        assertThat("Gauge is not registered correctly", registry.getGauges(), hasKey(GAUGE_NAME));
        @SuppressWarnings("unchecked")
        Gauge<Long> gauge = registry.getGauges().get(GAUGE_NAME);

        // Make sure that the gauge has the default value
        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(0L)));

        // Call the setter method
        long value = 1L + Math.round(Math.random() * (Long.MAX_VALUE - 1L));
        bean.setGauge(value);

        // Assert the gauge returns the cached value
        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(0L)));

        // Wait for two cache timeout periods
        Thread.sleep(2 * 500L);

        // Assert the gauge is refreshed and up-to-date
        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(value)));
    }
}