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
package org.stefanutti.metrics.cdi.ee;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class TimedMethodWithElNameBeanTest {

    private final static AtomicLong TIMER_COUNT = new AtomicLong();

    private String timerName;

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(EnterpriseArchive.class)
            .addAsLibraries(
                Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile("pom.xml")
                    .resolve("org.stefanutti.metrics.cdi:metrics-cdi")
                    .withTransitivity()
                    .as(JavaArchive.class)
            )
            .addAsLibrary(
                ShrinkWrap.create(JavaArchive.class)
                    .addClass(TimedMethodWithElNameBean.class)
                    .addClass(TimerIdBean.class)
                    // FIXME: Test class must be added until ARQ-659 is fixed
                    .addClass(TimedMethodWithElNameBeanTest.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            );
    }

    @Inject
    private MetricRegistry registry;
    
    @Inject
    private TimedMethodWithElNameBean bean;

    @Inject
    @Named("timerIdBean")
    private TimerIdBean timerIdBean;

    @Before
    public void getTimerName() {
        timerName = MetricRegistry.name(TimedMethodWithElNameBean.class, "timer " + timerIdBean.getId());
    }

    @Test
    @InSequence(1)
    public void timedMethodNotCalledYet() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerName));
        Timer timer = registry.getTimers().get(timerName);

        // Make sure that the timer hasn't been called yet
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(0L)));
    }

    @Test
    @InSequence(2)
    public void callExpressionTimedMethodOnce() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerName));
        Timer timer = registry.getTimers().get(timerName);

        // Call the timed method and assert it's been timed
        bean.expressionTimedMethod();

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(TIMER_COUNT.incrementAndGet())));
    }

    @Test
    @InSequence(3)
    public void callCompositeExpressionTimedMethodOnce() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerName));
        Timer timer = registry.getTimers().get(timerName);

        // Call the timed method and assert it's been timed
        bean.compositeExpressionTimedMethod();

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(TIMER_COUNT.incrementAndGet())));
    }

    @Test
    @InSequence(3)
    public void callLambdaExpressionTimedMethodOnce() {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(timerName));
        Timer timer = registry.getTimers().get(timerName);

        // Call the timed method and assert it's been timed
        bean.lambdaExpressionTimedMethod();

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(TIMER_COUNT.incrementAndGet())));
    }
}