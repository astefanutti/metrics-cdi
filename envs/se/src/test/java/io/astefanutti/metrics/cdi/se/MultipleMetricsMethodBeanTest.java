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
import io.astefanutti.metrics.cdi.se.util.MetricsUtil;

import javax.inject.Inject;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class MultipleMetricsMethodBeanTest {

    private final static String[] METRIC_NAMES = {"counter", "exception", "gauge", "meter", "timer"};

    private Set<String> absoluteMetricNames() {
        return MetricsUtil.absoluteMetricNameSet(MultipleMetricsMethodBean.class, METRIC_NAMES);
    }

    private String absoluteMetricName(String name) {
        return MetricsUtil.absoluteMetricName(MultipleMetricsMethodBean.class, name);
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(MultipleMetricsMethodBean.class)
            // Metrics CDI extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private MultipleMetricsMethodBean bean;

    @Before
    public void instantiateApplicationScopedBean() {
        // Let's trigger the instantiation of the application scoped bean explicitly
        // as only a proxy gets injected otherwise
        bean.toString();
    }

    @Test
    @InSequence(1)
    public void metricsMethodNotCalledYet() {
        assertThat("Metrics are not registered correctly", registry.getMetrics().keySet(), is(equalTo(absoluteMetricNames())));
    }

    @Test
    @InSequence(2)
    public void callMetricsMethodOnce() {
        assertThat("Metrics are not registered correctly", registry.getMetrics().keySet(), is(equalTo(absoluteMetricNames())));

        // Call the monitored method and assert it's been instrumented
        bean.metricsMethod();

        // Make sure that the metrics have been called
        assertThat("Counter count is incorrect", registry.getCounters().get(absoluteMetricName("counter")).getCount(), is(equalTo(1L)));
        assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName("exception")).getCount(), is(equalTo(0L)));
        assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName("meter")).getCount(), is(equalTo(1L)));
        assertThat("Timer count is incorrect", registry.getTimers().get(absoluteMetricName("timer")).getCount(), is(equalTo(1L)));
        // Let's call the gauge at the end as Weld is intercepting the gauge invocation while OWB not
        assertThat("Gauge value is incorrect", registry.getGauges().get(absoluteMetricName("gauge")).getValue(), hasToString((equalTo("value"))));
    }
}