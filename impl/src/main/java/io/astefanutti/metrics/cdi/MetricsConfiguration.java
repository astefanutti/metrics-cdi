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
package io.astefanutti.metrics.cdi;

/**
 * The Metrics CDI configuration. Metrics CDI fires a {@code MetricsConfiguration} event
 * during the deployment phase and that the application can observe to configure Metrics CDI.
 */
public interface MetricsConfiguration {

    /**
     * Overrides the Metrics annotation {@code absolute} attribute values globally for the application to use metric absolute names.
     *
     * @return This Metrics CDI configuration
     */
    MetricsConfiguration useAbsoluteName(boolean useAbsoluteName);
}
