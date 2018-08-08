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
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

@ApplicationScoped
public class HealthCheckProducerMethodBean {
	@Produces
	@Named("check1")
	HealthCheck aHealthyCheck() {
		return new HealthCheck() {
			@Override
			protected Result check() {
				return Result.healthy("check1");
			}
		};
	}

	@Produces
	@Named("check2")
	HealthCheck anUnhealthyCheck() {
		return new HealthCheck() {
			@Override
			protected Result check() throws Exception {
				return Result.unhealthy("check2");
			}
		};
	}

	@Produces
	@Named("not_registered_healthcheck")
	HealthCheck anInjectedCheck(HealthCheckRegistry registry, InjectionPoint ip) {
		HealthCheck check3 = new HealthCheck() {
			@Override
			protected Result check() throws Exception {
				return Result.healthy("check3");
			}
		};
		registry.register("check3", check3);
		return check3;
	}
}
