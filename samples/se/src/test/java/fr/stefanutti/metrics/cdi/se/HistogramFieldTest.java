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

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import fr.stefanutti.metrics.cdi.MetricsExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class HistogramFieldTest {

    private final static String HISTOGRAM_NAME = MetricRegistry.name(HistogramFieldBean.class, "histogramName");

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(HistogramFieldBean.class)
            // Metrics CDI extension
            .addPackages(false, MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource("META-INF/beans.xml", "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private HistogramFieldBean bean;

    @Test
    @InSequence(1)
    public void histogramFieldRegistered() {
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(HISTOGRAM_NAME));
    }

    @Test
    @InSequence(2)
    public void updateHistogramField() {
        assertThat("Histogram is not registered correctly", registry.getHistograms(), hasKey(HISTOGRAM_NAME));
        Histogram histogram = registry.getHistograms().get(HISTOGRAM_NAME);

        // Call the update method and assert the histogram is up-to-date
        long value = Math.round(Math.random() * Long.MAX_VALUE);
        bean.update(value);
        assertThat("Histogram count is incorrect", histogram.getCount(), is(equalTo(1L)));
        assertThat("Histogram size is incorrect", histogram.getSnapshot().size(), is(equalTo(1)));
        assertThat("Histogram min value is incorrect", histogram.getSnapshot().getMin(), is(equalTo(value)));
        assertThat("Histogram max value is incorrect", histogram.getSnapshot().getMax(), is(equalTo(value)));
    }
}