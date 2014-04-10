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


import com.codahale.metrics.*;
import org.stefanutti.metrics.cdi.Metric;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class MetricProducerFieldBean {

    @Produces
    @Metric(name = "counter1", absolute = true)
    private final Counter counter1 = new Counter();

    @Produces
    @Metric(name = "counter2", absolute = true)
    private final Counter counter2 = new Counter();

    @Produces
    @Metric(name = "ratioGauge", absolute = true)
    private final Gauge<Double> gauge = new RatioGauge() {
        @Override
        protected Ratio getRatio() {
            return Ratio.of(counter1.getCount(), counter2.getCount());
        }
    };

    @Produces
    private final Histogram histogram = new Histogram(new SlidingTimeWindowReservoir(1L, TimeUnit.SECONDS));
}
