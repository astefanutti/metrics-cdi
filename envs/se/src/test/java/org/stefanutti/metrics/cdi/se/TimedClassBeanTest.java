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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.hamcrest.Matchers;
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
import org.stefanutti.metrics.cdi.se.util.MetricsUtil;

import javax.inject.Inject;
import java.util.Set;

import static org.fest.reflect.core.Reflection.method;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class TimedClassBeanTest {

    private final static String[] TIMER_NAMES = {"timedMethodOne", "timedMethodTwo"};

    private Set<String> absoluteMetricNames() {
        return MetricsUtil.absoluteMetricNameSet(TimedClassBean.class.getPackage().getName() + "." + "timedClass", TIMER_NAMES);
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(TimedClassBean.class)
            // Metrics CDI extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private TimedClassBean bean;

    @Test
    @InSequence(1)
    public void timedMethodsNotCalledYet() {
        assertThat("Timers are not registered correctly", registry.getTimers().keySet(), is(equalTo(absoluteMetricNames())));

        // Make sure that the timers haven't been timed yet
        assertThat("Timer counts are incorrect", registry.getTimers().values(), everyItem(Matchers.<Timer>hasProperty("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callTimedMethodsOnce() {
        assertThat("Timers are not registered correctly", registry.getTimers().keySet(), is(equalTo(absoluteMetricNames())));

        // Call the timed methods and assert they've been timed
        bean.timedMethodOne();
        bean.timedMethodTwo();
        // Let's call the non-public methods as well
        bean.timedMethodProtected();
        bean.timedMethodPackagedPrivate();
        method("timedMethodPrivate").in(bean).invoke();

        // Make sure that the timers have been timed
        assertThat("Timer counts are incorrect", registry.getTimers().values(), everyItem(Matchers.<Timer>hasProperty("count", equalTo(1L))));
    }
}