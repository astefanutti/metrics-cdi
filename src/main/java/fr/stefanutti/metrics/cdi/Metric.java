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
package fr.stefanutti.metrics.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to provide metadata for a {@code com.codahale.metrics.Metric} injection.
 * <p/>
 * Given the declared field:
 * <pre><code>
 *     \@Inject
 *     \@Metric
 *     public Timer timer;
 * </code></pre>
 * <p/>
 * The timer for the defining bean with the name {@code timer} will be injected. It will be up to the user
 * to time the injected timer. This annotation can be used on fields and arguments of type
 * Meter, Timer, Counter, and Histogram.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Metric {

    /**
     * The name of the metric. If not specified, the name will be based on the annotated member.
     */
    String name() default "";

    /**
     * If {@code true}, use the given name an as absolute name. If {@code false}, use the given name
     * relative to the class of the annotated member.
     */
    boolean absolute() default false;
}
