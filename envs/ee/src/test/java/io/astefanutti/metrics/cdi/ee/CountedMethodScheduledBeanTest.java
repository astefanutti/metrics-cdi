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
package io.astefanutti.metrics.cdi.ee;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import io.astefanutti.metrics.cdi.ee.categories.Integration;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class CountedMethodScheduledBeanTest {

    private final static String COUNTER_NAME = MetricRegistry.name(CountedMethodScheduledBean.class, "count");

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(EnterpriseArchive.class)
            .addAsLibraries(
                Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile("pom.xml")
                    .resolve("io.astefanutti.metrics.cdi:metrics-cdi")
                    .withTransitivity()
                    .as(JavaArchive.class))
            .addAsModule(
                ShrinkWrap.create(JavaArchive.class)
                    .addClass(CountedMethodScheduledBean.class)
                    .addClass(CallCounter.class)
                    // FIXME: Test class must be added until ARQ-659 is fixed
                    .addClass(CountedMethodScheduledBeanTest.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"));
    }

    @Inject
    private MetricRegistry registry; 

    @Inject
    private CallCounter callCounter; 

    @Before
    public void init() {
        callCounter.reset();
    }

    @Test
    public void testAScheduleMethodCanBeTimed() throws InterruptedException {
        // let's wait a few seconds, so that the scheduled method has been fired
        Thread.sleep(3000l);

        assertThat("Scheduled counter is not registered correctly", registry.getCounters(), hasKey(COUNTER_NAME));
        Counter counter = registry.getCounters().get(COUNTER_NAME);

        // Make sure that the scheduled method has been called
        assertThat("Scheduled method of EJB has not been called", callCounter.value(), greaterThan(0l));

        // Make sure that the interception occurred
        assertThat("Counter count is incorrect", counter.getCount(), greaterThan(0l));
    }
}
