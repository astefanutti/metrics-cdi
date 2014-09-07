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
package io.astefanutti.metrics.cdi.ee;

import com.codahale.metrics.MetricRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class TimerFieldWithElNameBeanTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(EnterpriseArchive.class)
            .addAsLibraries(
                Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile("pom.xml")
                    .resolve("io.astefanutti.metrics.cdi:metrics-cdi")
                    .withTransitivity()
                    .as(JavaArchive.class)
                )
            .addAsLibrary(
                ShrinkWrap.create(JavaArchive.class)
                    .addClass(TimerFieldWithElNameBean.class)
                    .addClass(TimerIdBean.class)
                    // FIXME: Test class must be added until ARQ-659 is fixed
                    .addClass(TimerFieldWithElNameBeanTest.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            );
    }

    @Inject
    private MetricRegistry registry;

    @Inject
    private TimerFieldWithElNameBean bean;

    @Inject
    @Named("timerIdBean")
    private TimerIdBean timerIdBean;

    @Test
    public void timerFieldsWithElNames() {
        assertThat("Timers are not registered correctly", registry.getMetrics(), allOf(
            hasKey("timer " + timerIdBean.getId() + " is absolute"),
            hasKey(MetricRegistry.name(TimerFieldWithElNameBean.class, "timer " + timerIdBean.getId()))));
    }
}