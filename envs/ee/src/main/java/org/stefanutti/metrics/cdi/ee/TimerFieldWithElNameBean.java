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
package org.stefanutti.metrics.cdi.ee;

import com.codahale.metrics.Timer;
import org.stefanutti.metrics.cdi.Metric;

import javax.inject.Inject;

public class TimerFieldWithElNameBean {

    @Inject
    @Metric(name = "${(id -> 'timer' += id)(timerIdBean.id)}")
    Timer timerWithName;

    @Inject
    @Metric(name = "${(id -> 'timer' += id)(timerIdBean.id)}", absolute = true)
    Timer timerWithAbsoluteName;
}