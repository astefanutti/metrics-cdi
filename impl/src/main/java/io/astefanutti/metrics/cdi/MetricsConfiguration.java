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
package io.astefanutti.metrics.cdi;

import com.codahale.metrics.Metric;
import com.codahale.metrics.Reservoir;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * The Metrics CDI configuration. Metrics CDI fires a {@code MetricsConfiguration} event
 * during the deployment phase that the application can observe and use to configure it.
 *
 * Note that the event fired can only be used within the observer method invocation context. Any attempt to call one of its methods outside of that context will result in an `IllegalStateException` to be thrown.
 */
public interface MetricsConfiguration {

    /**
     * Overrides the Metrics annotation {@code absolute} attribute values globally for the application to use metric absolute names.
     *
     * @param useAbsoluteName the value whether to use absolute name
     * @return this Metrics CDI configuration
     * @throws IllegalStateException if called outside of the observer method invocation
     */
    MetricsConfiguration useAbsoluteName(boolean useAbsoluteName);

    /**
     * Registers a function that supplies a {@link com.codahale.metrics.Reservoir} instance depending on the metric.
     *
     * @param function the {@code BiFunction} that supplies the {@link com.codahale.metrics.Reservoir} instances
     * @return this Metrics CDI configuration
     * @throws IllegalStateException if called outside of the observer method invocation
     * @since 1.5.0
     */
    MetricsConfiguration reservoirFunction(BiFunction<String, Class<? extends Metric>, Optional<Reservoir>> function);
}
