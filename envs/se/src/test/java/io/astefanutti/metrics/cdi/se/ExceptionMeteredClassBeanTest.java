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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
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
import io.astefanutti.metrics.cdi.MetricsExtension;
import io.astefanutti.metrics.cdi.se.util.MetricsUtil;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.fest.reflect.core.Reflection.method;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class ExceptionMeteredClassBeanTest {

    private final static String[] METER_NAMES = {"exceptionMeteredMethodOne.exceptions", "exceptionMeteredMethodTwo.exceptions"};

    private final static AtomicLong[] METER_COUNTS = {new AtomicLong(), new AtomicLong()};

    private Set<String> absoluteMetricNames() {
        return MetricsUtil.absoluteMetricNameSet(ExceptionMeteredClassBean.class.getPackage().getName() + "." + "exceptionMeteredClass", METER_NAMES);
    }

    private static String absoluteMetricName(int index) {
        return MetricsUtil.absoluteMetricName(ExceptionMeteredClassBean.class.getPackage().getName() + "." + "exceptionMeteredClass", METER_NAMES[index]);
    }

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
        assertThat("Meters are not registered correctly", registry.getMeters().keySet(), is(equalTo(absoluteMetricNames())));

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
        method("exceptionMeteredMethodPrivate").withParameterTypes(Runnable.class).in(bean).invoke(runnableThatDoesNoThrowExceptions);

        assertThat("Meters counts are incorrect", registry.getMeters().values(), everyItem(Matchers.<Meter>hasProperty("count", equalTo(0L))));
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
            bean.exceptionMeteredMethodOne(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(0)).getCount(), is(equalTo(METER_COUNTS[0].incrementAndGet())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            // Call the metered method and assert it's been marked and that the original exception has been rethrown
            bean.exceptionMeteredMethodTwo(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(1)).getCount(), is(equalTo(METER_COUNTS[1].incrementAndGet())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        // Let's call the non-public methods as well
        try {
            bean.exceptionMeteredMethodProtected(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meters counts are incorrect", registry.getMeters().values(), everyItem(Matchers.<Meter>hasProperty("count", allOf(equalTo(METER_COUNTS[0].get()), equalTo(METER_COUNTS[1].get())))));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            bean.exceptionMeteredMethodPackagedPrivate(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meters counts are incorrect", registry.getMeters().values(), everyItem(Matchers.<Meter>hasProperty("count", allOf(equalTo(METER_COUNTS[0].get()), equalTo(METER_COUNTS[1].get())))));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            method("exceptionMeteredMethodPrivate").withParameterTypes(Runnable.class).in(bean).invoke(runnableThatThrowsIllegalArgumentException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meters counts are incorrect", registry.getMeters().values(), everyItem(Matchers.<Meter>hasProperty("count", allOf(equalTo(METER_COUNTS[0].get()), equalTo(METER_COUNTS[1].get())))));
            assertSame("Exception thrown is incorrect", cause, exception);
        }
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
            bean.exceptionMeteredMethodOne(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(0)).getCount(), is(equalTo(METER_COUNTS[0].get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            // Call the metered method and assert it hasn't been marked and that the original exception has been rethrown
            bean.exceptionMeteredMethodTwo(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meter count is incorrect", registry.getMeters().get(absoluteMetricName(1)).getCount(), is(equalTo(METER_COUNTS[1].get())));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        // Let's call the non-public methods as well
        try {
            bean.exceptionMeteredMethodProtected(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meters counts are incorrect", registry.getMeters().values(), everyItem(Matchers.<Meter>hasProperty("count", allOf(equalTo(METER_COUNTS[0].get()), equalTo(METER_COUNTS[1].get())))));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            bean.exceptionMeteredMethodPackagedPrivate(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meters counts are incorrect", registry.getMeters().values(), everyItem(Matchers.<Meter>hasProperty("count", allOf(equalTo(METER_COUNTS[0].get()), equalTo(METER_COUNTS[1].get())))));
            assertSame("Exception thrown is incorrect", cause, exception);
        }

        try {
            method("exceptionMeteredMethodPrivate").withParameterTypes(Runnable.class).in(bean).invoke(runnableThatThrowsIllegalStateException);
            fail("No exception has been re-thrown!");
        } catch (RuntimeException cause) {
            assertThat("Meters counts are incorrect", registry.getMeters().values(), everyItem(Matchers.<Meter>hasProperty("count", allOf(equalTo(METER_COUNTS[0].get()), equalTo(METER_COUNTS[1].get())))));
            assertSame("Exception thrown is incorrect", cause, exception);
        }
    }
}