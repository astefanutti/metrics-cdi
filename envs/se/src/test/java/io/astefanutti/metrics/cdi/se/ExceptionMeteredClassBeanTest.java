/**
 * Copyright © 2013 Antonin Stefanutti (antonin.stefanutti@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.astefanutti.metrics.cdi.se;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.astefanutti.metrics.cdi.MetricsExtension;
import io.astefanutti.metrics.cdi.se.util.MetricsUtil;
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

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ExceptionMeteredClassBeanTest {

    private static final String[] METHOD_NAMES = {"exceptionMeteredMethodOne.exceptions", "exceptionMeteredMethodTwo.exceptions", "exceptionMeteredMethodProtected.exceptions", "exceptionMeteredMethodPackagedPrivate.exceptions"};

    private static final String CONSTRUCTOR_NAME = "ExceptionMeteredClassBean.exceptions";

    private static final Set<String> METHOD_METER_NAMES = MetricsUtil.absoluteMetricNames(ExceptionMeteredClassBean.class, "exceptionMeteredClass", METHOD_NAMES);

    private static final MetricFilter METHOD_METERS = new MetricFilter() {
        @Override
        public boolean matches(String name, Metric metric) {
            return METHOD_METER_NAMES.contains(name);
        }
    };

    private static String absoluteMetricName(int index) {
        return MetricsUtil.absoluteMetricName(ExceptionMeteredClassBean.class, "exceptionMeteredClass", METHOD_NAMES[index]);
    }

    private static final String CONSTRUCTOR_METER_NAME = MetricsUtil.absoluteMetricName(ExceptionMeteredClassBean.class, "exceptionMeteredClass", CONSTRUCTOR_NAME);

    private static final Set<String> METER_NAMES = MetricsUtil.absoluteMetricNames(ExceptionMeteredClassBean.class, "exceptionMeteredClass", METHOD_NAMES, CONSTRUCTOR_NAME);

    private static final AtomicLong METHOD_COUNT = new AtomicLong();

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(ExceptionMeteredClassBean.class)
            // Metrics CDI extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private ExceptionMeteredClassBean bean;

    @Test
    @InSequence(1)
    public void callExceptionMeteredMethodsOnceWithoutThrowing() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(METER_NAMES)));

        assertThat("Constructor meter count is incorrect", registry.getMeters().get(CONSTRUCTOR_METER_NAME).getCount(), is(equalTo(0L)));

        Runnable runnableThatDoesNoThrowExceptions = new Runnable() {
            @Override
            public void run() {
            }
        };

        // Call the metered methods and assert they haven't been marked
        bean.exceptionMeteredMethodOne(runnableThatDoesNoThrowExceptions);
        bean.exceptionMeteredMethodTwo(runnableThatDoesNoThrowExceptions);
        // Let's call the non-public methods as well
        bean.exceptionMeteredMethodProtected(runnableThatDoesNoThrowExceptions);
        bean.exceptionMeteredMethodPackagedPrivate(runnableThatDoesNoThrowExceptions);

        // Make sure that the method meters haven't been marked yet
        assertThat("Method meter counts are incorrect", registry.getMeters(METHOD_METERS).values(), everyItem(Matchers.<Meter>hasProperty("count", equalTo(0L))));
    }

    @Test
    @InSequence(2)
    public void callExceptionMeteredMethodOnceWithThrowingExpectedException() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(METER_NAMES)));

        assertThat("Constructor meter count is incorrect", registry.getMeters().get(CONSTRUCTOR_METER_NAME).getCount(), is(equalTo(0L)));

        final RuntimeException exception = new IllegalArgumentException("message");
        Runnable runnableThatThrowsIllegalArgumentException = new Runnable() {
            @Override
            public void run() {
                throw exception;
            }
        };

        try {
            // Call the metered method and assert it's been marked and that the original exception has been rethrown
            bean.exceptionMeteredMethodOne(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(0)).getCount(), is(equalTo(METHOD_COUNT.incrementAndGet())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            bean.exceptionMeteredMethodTwo(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(1)).getCount(), is(equalTo(METHOD_COUNT.get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            bean.exceptionMeteredMethodProtected(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(2)).getCount(), is(equalTo(METHOD_COUNT.get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            bean.exceptionMeteredMethodPackagedPrivate(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(3)).getCount(), is(equalTo(METHOD_COUNT.get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }
    }

    @Test
    @InSequence(3)
    public void callExceptionMeteredMethodOnceWithThrowingNonExpectedException() {
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(METER_NAMES)));

        assertThat("Constructor meter count is incorrect", registry.getMeters().get(CONSTRUCTOR_METER_NAME).getCount(), is(equalTo(0L)));

        final RuntimeException exception = new IllegalStateException("message");
        Runnable runnableThatThrowsIllegalStateException = new Runnable() {
            @Override
            public void run() {
                throw exception;
            }
        };

        try {
            // Call the metered method and assert it hasn't been marked and that the original exception has been rethrown
            bean.exceptionMeteredMethodOne(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(0)).getCount(), is(equalTo(METHOD_COUNT.get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            bean.exceptionMeteredMethodTwo(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(1)).getCount(), is(equalTo(METHOD_COUNT.get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        // Let's call the non-public methods as well
        try {
            bean.exceptionMeteredMethodProtected(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(2)).getCount(), is(equalTo(METHOD_COUNT.get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            bean.exceptionMeteredMethodPackagedPrivate(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(3)).getCount(), is(equalTo(METHOD_COUNT.get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }
    }
}