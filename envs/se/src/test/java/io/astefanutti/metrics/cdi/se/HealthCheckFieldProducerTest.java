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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.astefanutti.metrics.cdi.MetricsExtension;
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
import java.util.SortedMap;

import static com.codahale.metrics.health.HealthCheck.Result;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class HealthCheckFieldProducerTest {

    private final static String HEALTH_CHECK_NAME = MetricRegistry.name(HealthCheckProducerFieldBean.class, "check3");

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(HealthCheckProducerFieldBean.class)
            // Metrics CDI Extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private HealthCheckRegistry registry;

    @Test
    @InSequence(1)
    public void healthChecksRegistered() {
        assertThat("HealthChecks are not registered correctly", registry.getNames(),
                containsInRelativeOrder("check1", "check2", HEALTH_CHECK_NAME));

        SortedMap<String, Result> results = registry.runHealthChecks();

        assertThat("check1 did not execute", results, hasKey("check1"));
        assertThat("check1 did not pass", results.get("check1").isHealthy(), is(true));

        assertThat("check2 did not execute", results, hasKey("check2"));
        assertThat("check2 did not fail", results.get("check2").isHealthy(), is(false));

        assertThat("check3 did not execute", results, hasKey(HEALTH_CHECK_NAME));
        assertThat("check3 did not pass", results.get(HEALTH_CHECK_NAME).isHealthy(), is(true));
    }
}
