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
package io.astefanutti.metrics.cdi.servlet;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.weld.environment.servlet.Listener;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class TimedMethodBeanTestJava8 {

    private final static String TIMER_NAME = MetricRegistry.name(TimedMethodBean.class, "timedMethod");

    private final static long CALL_COUNT = 1L + Math.round(Math.random() * 10);

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
            // TODO: understand why this is duplicated in the Archive and fails since Weld 2.2.5.Final
            /* .addAsLibraries(
                Maven.configureResolver()
                    .workOffline()
                    .loadPomFromFile("pom.xml")
                    .resolve("io.astefanutti.metrics.cdi:metrics-cdi", "org.jboss.weld.servlet:weld-servlet")
                    .withTransitivity()
                    .as(JavaArchive.class)) */
            .addClass(TimedMethodBean.class)
            .addClass(TimedMethodServlet.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
            .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
                .version("3.1")
                .createServlet()
                    .servletClass(TimedMethodServlet.class.getName())
                    .servletName("TimedMethod").up()
                .createServletMapping()
                    .servletName("TimedMethod")
                    .urlPattern("/timedMethod").up()
                .createListener()
                    .listenerClass(Listener.class.getName()).up()
                .exportAsString()));
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void timedMethodNotCalledYet(@ArquillianResource URL url) throws IOException {
        for (int i = 0; i < CALL_COUNT; i++)
            readStreamAndClose(new URL(url, "timedMethod").openStream());
    }

    @Test
    @InSequence(2)
    public void timedMethodCalled(MetricRegistry registry) {
        assertThat("Timer is not registered correctly", registry.getTimers(), hasKey(TIMER_NAME));
        Timer timer = registry.getTimers().get(TIMER_NAME);

        // Make sure that the timer has been called
        assertThat("Timer count is incorrect", timer.getCount(), is(equalTo(CALL_COUNT)));
    }

    private String readStreamAndClose(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            int read;
            while( (read = is.read()) != -1)
                os.write(read);
        } finally {
            is.close();
        }
        return os.toString();
    }
}