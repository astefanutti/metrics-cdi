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

/**
 * Builder class allowing the production of different {@link Reservoir} implementations based on the metric kind & names.
 */
public interface ReservoirBuidler {
    /**
     * Builds a {@link Reservoir} instance to be used for the given metric
     * @param name the metric name
     * @param type the metric class
     * @return the reservoir to use, or an empty {@code Optional} instance if default reservoir implementation has to be used
     */
    Optional<Reservoir> build(String name, Class<? extends Metric> type);
}
