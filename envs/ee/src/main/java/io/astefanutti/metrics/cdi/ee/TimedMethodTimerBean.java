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
package io.astefanutti.metrics.cdi.ee;

import com.codahale.metrics.annotation.Timed;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@Startup
@Singleton
public class TimedMethodTimerBean {

    @Resource
    private TimerService ts;

    @Inject
    CallCounter counter;

    @PostConstruct
    public void init() {
        ts.createIntervalTimer(0l, TimeUnit.SECONDS.toMillis(1), new TimerConfig("a test timer", false));
    }

    @Timeout
    @Timed(name = "schedule")
    public void scheduledMethod() {
        counter.count();
    }
}
