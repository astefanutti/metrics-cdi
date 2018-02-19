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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.astefanutti.metrics.cdi.MetricsExtension;

@RunWith(Arquillian.class)
public class ReservoirBuilderUniformReservoirTest {

    @Inject
    ReservoirBuilderUniformContributor reservoirBuilder;
    
    @Inject
    TimerFieldBean timerBean;

    @Inject
    HistogramFieldBean histogramBean;

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            // Test bean class with Timer injection
            .addClass(TimerFieldBean.class)
            // Test bean class with Histogram injection
            .addClass(HistogramFieldBean.class)
            // The ReservoirBuilder counting calls and returning UniformReservoir
            .addClass(ReservoirBuilderUniformContributor.class)
            // Metrics CDI extension
            .addPackage(MetricsExtension.class.getPackage())
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void checkReservoirBuilderCalls() {
        int NB_TIMER_FIELDS_IN_BEAN = 6;
        int NB_HISTOGRAM_FIELDS_IN_BEAN = 1;

        int expectedCalls = NB_TIMER_FIELDS_IN_BEAN + NB_HISTOGRAM_FIELDS_IN_BEAN;
        assertThat("Number of ReservoirBuilder calls differ from expected # of Timer fields", reservoirBuilder.calls(), is(expectedCalls));
    }
}
