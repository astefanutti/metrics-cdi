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
package io.astefanutti.metrics.cdi.se;

import com.codahale.metrics.annotation.Timed;

public class VisibilityTimedMethodBean {

    @Timed
    public void publicTimedMethod() {
    }

    @Timed
    void packagePrivateTimedMethod() {
    }

    @Timed
    protected void protectedTimedMethod() {
    }

    //@Timed
    // It appears that OWB does not support interception of private method while Weld does.
    // Neither the CDI nor Java Interceptors specifications make that point explicit though
    // document like http://docs.jboss.org/webbeans/spec/PDR/html/interceptors.html is stating
    // that for a method invocation to be considered a business method invocation the method
    // must be non-private and non-static.
    private void privateTimedMethod() {
    }
}
