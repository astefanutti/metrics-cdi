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
package org.stefanutti.metrics.cdi.se;

import com.codahale.metrics.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.stefanutti.metrics.cdi.MetricsExtension;

import javax.inject.Inject;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class MetricProducerMethodBeanTest {

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(MetricProducerMethodBean.class)
            // Metrics CDI extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private MetricProducerMethodBean bean;

    @Test
    @InSequence(1)
    public void cachedMethodNotCalledYet() {
        assertThat("Metrics are not registered correctly", registry.getMetrics(), allOf(hasKey("calls"), hasKey("hits"), hasKey("cache-hits")));
        Timer calls = registry.getTimers().get("calls");
        Meter hits = registry.getMeters().get("hits");
        @SuppressWarnings("unchecked")
        Gauge<Double> gauge = (Gauge<Double>) registry.getGauges().get("cache-hits");

        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo(((double) hits.getCount() / (double) calls.getCount()))));
    }

    @Test
    @InSequence(2)
    public void callCachedMethodMultipleTimes() {
        assertThat("Metrics are not registered correctly", registry.getMetrics(), allOf(hasKey("calls"), hasKey("hits"), hasKey("cache-hits")));
        Timer calls = registry.getTimers().get("calls");
        Meter hits = registry.getMeters().get("hits");
        @SuppressWarnings("unchecked")
        Gauge<Double> gauge = (Gauge<Double>) registry.getGauges().get("cache-hits");

        long count = 10 + Math.round(Math.random() * 10);
        for (int i = 0; i < count; i++)
            bean.cachedMethod((Math.random() < 0.5));

        assertThat("Gauge value is incorrect", gauge.getValue(), is(equalTo((double) hits.getCount() / (double) calls.getCount())));
    }
}