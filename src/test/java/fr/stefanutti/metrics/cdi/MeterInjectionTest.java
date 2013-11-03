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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import fr.stefanutti.metrics.cdi.bean.MeteredMethodBean;
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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class MeterInjectionTest {

    @Deployment
    private static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(MeteredMethodBean.class)
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
    private MeteredMethodBean bean;

    @Inject
    @Metric(absolute = true, name = "fr.stefanutti.metrics.cdi.bean.MeteredMethodBean.meteredMethod")
    private Meter meter;

    @Test
    @InSequence(1)
    public void meteredMethodNotCalledYet() {
        // Make sure that the meter hasn't been called yet
        assertThat("Meter count is incorrect", meter.getCount(), is(equalTo(0L)));
    }

    @Test
    @InSequence(2)
    public void callMeteredMethodOnce() {
        // Call the metered method and assert it's been marked
        bean.meteredMethod();

        // Make sure that the meter has been called
        assertThat("Meter count is incorrect", meter.getCount(), is(equalTo(1L)));
    }
}