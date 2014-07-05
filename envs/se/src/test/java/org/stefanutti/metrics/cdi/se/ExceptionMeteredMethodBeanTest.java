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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.stefanutti.metrics.cdi.MetricsExtension;
import org.stefanutti.metrics.cdi.se.util.MetricsUtil;
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
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class ExceptionMeteredMethodBeanTest {

    private final static String[] METER_NAMES = {"illegalArgumentExceptionMeteredMethod", "exceptionMeteredMethod"};

    private final static AtomicLong[] METER_COUNTS = {new AtomicLong(), new AtomicLong()};

    private Set<String> absoluteMetricNames() {
        return MetricsUtil.absoluteMetricNameSet(ExceptionMeteredMethodBean.class, METER_NAMES);
    }

    private static String absoluteMetricName(int index) {
        return MetricsUtil.absoluteMetricName(ExceptionMeteredMethodBean.class, METER_NAMES[index]);
    }

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(ExceptionMeteredMethodBean.class)
            // Metrics CDI extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private ExceptionMeteredMethodBean bean;

    @Test
    @InSequence(1)
    public void callExceptionMeteredMethodsOnceWithoutThrowing() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(absoluteMetricNames())));

        Runnable runnableThatDoesNoThrowExceptions = new Runnable() {
            @Override
            public void run() {
            }
        };

        // Call the metered methods and assert they haven't been marked
        bean.illegalArgumentExceptionMeteredMethod(runnableThatDoesNoThrowExceptions);
        bean.exceptionMeteredMethod(runnableThatDoesNoThrowExceptions);

        assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(0)).getCount(), is(equalTo(METER_COUNTS[0].get())));
        assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(1)).getCount(), is(equalTo(METER_COUNTS[1].get())));
    }

    @Test
    @InSequence(2)
    public void callExceptionMeteredMethodOnceWithThrowingExpectedException() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(absoluteMetricNames())));

        final RuntimeException exception = new IllegalArgumentException("message");
        Runnable runnableThatThrowsIllegalArgumentException = new Runnable() {
            @Override
            public void run() {
                throw exception;
            }
        };

        try {
            // Call the metered method and assert it's been marked and that the original exception has been rethrown
            bean.illegalArgumentExceptionMeteredMethod(runnableThatThrowsIllegalArgumentException);
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(0)).getCount(), is(equalTo(METER_COUNTS[0].incrementAndGet())));
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(1)).getCount(), is(equalTo(METER_COUNTS[1].get())));
            assertSame("Exception thrown is incorrect", cause, exception);
            return;
        }

        fail("No exception has been re-thrown!");
    }

    @Test
    @InSequence(3)
    public void callExceptionMeteredStaticMethodOnceWithThrowingNonExpectedException() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(absoluteMetricNames())));

        final RuntimeException exception = new IllegalStateException("message");
        Runnable runnableThatThrowsIllegalStateException = new Runnable() {
            @Override
            public void run() {
                throw exception;
            }
        };

        try {
            // Call the metered method and assert it hasn't been marked and that the original exception has been rethrown
            bean.illegalArgumentExceptionMeteredMethod(runnableThatThrowsIllegalStateException);
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(0)).getCount(), is(equalTo(METER_COUNTS[0].get())));
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(1)).getCount(), is(equalTo(METER_COUNTS[1].get())));
            assertSame("Exception thrown is incorrect", cause, exception);
            return;
        }

        fail("No exception has been re-thrown!");
    }

    @Test
    @InSequence(4)
    public void callExceptionMeteredStaticMethodOnceWithThrowingInstanceOfExpectedException() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(absoluteMetricNames())));

        final RuntimeException exception = new IllegalStateException("message");
        Runnable runnableThatThrowsIllegalStateException = new Runnable() {
            @Override
            public void run() {
                throw exception;
            }
        };

        try {
            // Call the metered method and assert it's been marked and that the original exception has been rethrown
            bean.exceptionMeteredMethod(runnableThatThrowsIllegalStateException);
        } catch (RuntimeException cause) {
            assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(absoluteMetricNames())));
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(0)).getCount(), is(equalTo(METER_COUNTS[0].get())));
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(1)).getCount(), is(equalTo(METER_COUNTS[1].incrementAndGet())));
            assertSame("Exception thrown is incorrect", cause, exception);
            return;
        }

        fail("No exception has been re-thrown!");
    }

    @Test
    @InSequence(5)
    public void removeMeterFromRegistry() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(absoluteMetricNames())));

        Meter meter = registry.getMeters().get(absoluteMetricName(0));

        // Remove the meter from metrics registry
        registry.remove(absoluteMetricName(0));

        final RuntimeException exception = new IllegalArgumentException("message");
        Runnable runnableThatThrowsIllegalArgumentException = new Runnable() {
            @Override
            public void run() {
                throw exception;
            }
        };

        try {
            // Call the metered method and assert an exception is thrown
            bean.illegalArgumentExceptionMeteredMethod(runnableThatThrowsIllegalArgumentException);
        } catch (RuntimeException cause) {
            assertThat(cause, is(instanceOf(IllegalStateException.class)));
            assertThat(cause.getMessage(), is(equalTo("No meter with name [" + absoluteMetricName(0) + "] found in registry [" + registry + "]")));
            // Make sure that the meter hasn't been marked
            assertThat("Meter count is incorrect", meter.getCount(), is(equalTo(METER_COUNTS[0].get())));
            return;
        }

        fail("No exception has been re-thrown!");
    }
}