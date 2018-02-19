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


import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/* package-private */ final class MetricsConfigurationEvent implements MetricsConfiguration {

    private final EnumMap<MetricsParameter, Object> configuration = new EnumMap<>(MetricsParameter.class);

    private volatile boolean unmodifiable;

    @Override
    public MetricsConfiguration useAbsoluteName(boolean useAbsoluteName) {
        throwsIfUnmodifiable();
        configuration.put(MetricsParameter.useAbsoluteName, Boolean.valueOf(useAbsoluteName));
        return this;
    }

    @Override
    public MetricsConfiguration useReservoirBuilder(ReservoirBuidler builder) {
        throwsIfUnmodifiable();
        configuration.put(MetricsParameter.useReservoirBuilder, builder);
        return this;
    }

    Map<MetricsParameter, Object> getParameters() {
        return Collections.unmodifiableMap(configuration);
    }

    void unmodifiable() {
        unmodifiable = true;
    }

    private void throwsIfUnmodifiable() {
        if (unmodifiable)
            throw new IllegalStateException("Metrics CDI configuration event must not be used outside its observer method!");
    }
}
