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
import com.codahale.metrics.Timer;
import fr.stefanutti.metrics.cdi.MetricsExtension;
import fr.stefanutti.metrics.cdi.se.util.MetricsUtil;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Set;

import static org.fest.reflect.core.Reflection.method;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class VisibilityTimedMethodTest {

    private final static String[] TIMER_NAMES = {"publicTimedMethod", "packagePrivateTimedMethod", "protectedTimedMethod", "privateTimedMethod"};

    private Set<String> absoluteMetricNames() {
        return MetricsUtil.absoluteMetricNameSet(VisibilityTimedMethodBean.class, TIMER_NAMES);
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(VisibilityTimedMethodBean.class)
            // Metrics CDI extension
            .addPackages(false, MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource("beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private VisibilityTimedMethodBean bean;

    @Test
    @InSequence(1)
    public void timedMethodsNotCalledYet() {
        assertThat("Timers are not registered correctly", registry.getTimers().keySet(), is(equalTo(absoluteMetricNames())));

        // Make sure that all the timers haven't been called yet
        assertThat("Timer counts are incorrect", registry.getTimers().values(), everyItem(Matchers.<Timer>hasProperty("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callTimedMethodsOnce() {
        assertThat("Timers are not registered correctly", registry.getTimers().keySet(), is(equalTo(absoluteMetricNames())));

        // Call the timed methods and assert they've all been timed once
        bean.publicTimedMethod();
        bean.protectedTimedMethod();
        bean.packagePrivateTimedMethod();
        method("privateTimedMethod").in(bean).invoke();
        // FIXME: check the interceptors specification for private method interception
        //assertThat("Timer counts are incorrect", registry.getTimers().values(), everyItem(Matchers.<Timer>hasProperty("count", equalTo(1L))));
        assertThat("Timer count is incorrect", registry.getTimers().get(MetricRegistry.name(VisibilityTimedMethodBean.class, "publicTimedMethod")).getCount(), is(equalTo(1L)));
        assertThat("Timer count is incorrect", registry.getTimers().get(MetricRegistry.name(VisibilityTimedMethodBean.class, "packagePrivateTimedMethod")).getCount(), is(equalTo(1L)));
        assertThat("Timer count is incorrect", registry.getTimers().get(MetricRegistry.name(VisibilityTimedMethodBean.class, "protectedTimedMethod")).getCount(), is(equalTo(1L)));
    }
}