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
import io.astefanutti.metrics.cdi.MetricsConfiguration;
import io.astefanutti.metrics.cdi.MetricsExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class UseAbsoluteNameTest {

    private final static String[] ABSOLUTE_METRIC_NAMES = {"timerWithoutAnnotation", "timerWithExplicitNonAbsoluteName", "timerWithNoName", "timerName", "timerWithAbsoluteDefaultName", "timerAbsoluteName"};

    private Set<String> metricNames() {
        return new HashSet<>(Arrays.asList(ABSOLUTE_METRIC_NAMES));
    }

    @Deployment
    static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean
            .addClass(TimerFieldBean.class)
            // Metrics CDI extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    static void configuration(@Observes MetricsConfiguration configuration) {
        configuration.useAbsoluteName(true);
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private TimerFieldBean bean;

    @Test
    public void timerFieldsWithDefaultNamingConvention() {
        assertThat("Timers are not registered correctly", registry.getMetrics().keySet(), is(equalTo(metricNames())));
    }
}