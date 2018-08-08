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

import com.codahale.metrics.health.HealthCheck;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import java.util.concurrent.atomic.AtomicLong;

import static io.astefanutti.metrics.cdi.se.HealthCheckBean.NAME;

@ApplicationScoped
@Named(NAME)
public class HealthCheckBean extends HealthCheck {

	static final String NAME = "HealthCheckBean";

	private AtomicLong checkCount = new AtomicLong(0l);

	@Override
	public Result check() {
		checkCount.incrementAndGet();
		return Result.healthy();
	}

	public Long getCheckCount() {
		return checkCount.get();
	}
}
