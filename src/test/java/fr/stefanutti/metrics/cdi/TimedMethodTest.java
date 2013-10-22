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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import fr.stefanutti.metrics.cdi.bean.MeteredMethodBean;
import fr.stefanutti.metrics.cdi.bean.TimedMethodBean;
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
public class TimedMethodTest {

    private final static String TIMER_NAME = TimedMethodBean.class.getName() + "." + "timedMethod";

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(TimedMethodBean.class)
            // DeltaSpike
            .addPackages(true, "org.apache.deltaspike.core.impl")
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
    private TimedMethodBean bean;

    @Test
    public void callTimedMethodOnce() {
        // Call the timed method and assert it's been timed
        bean.timedMethod();

        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(TIMER_NAME));
        Timer timer = registry.getTimers().get(TIMER_NAME);

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(1L)));
    }
}