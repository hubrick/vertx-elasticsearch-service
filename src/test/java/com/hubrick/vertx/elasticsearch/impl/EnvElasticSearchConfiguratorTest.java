/**
 * Copyright (C) 2016 Etaia AS (oss@hubrick.com)
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
package com.hubrick.vertx.elasticsearch.impl;

import io.vertx.core.json.JsonObject;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EnvElasticSearchConfiguratorTest {

    private final String ENV_TRANSPORT_ADDRESS = EnvElasticSearchConfigurator.ENV_VAR_TRANSPORT_ADDRESSES;

    @Test
    public void testInitTransportAddressesWhenSingleNoPort() throws Exception {
        setEnv(Collections.singletonMap(ENV_TRANSPORT_ADDRESS, "172.45.0.1"));

        final EnvElasticSearchConfigurator envConfigurator = new EnvElasticSearchConfigurator(new JsonObject());

        final List<TransportAddress> expected =
                Collections.singletonList(new InetSocketTransportAddress(new InetSocketAddress("172.45.0.1", 9300)));

        final List<TransportAddress> actual = envConfigurator.transportAddresses;

        assertEquals(expected, actual);
    }

    @Test
    public void testInitTransportAddressesWhenSingleHasPort() throws Exception {
        setEnv(Collections.singletonMap(ENV_TRANSPORT_ADDRESS, "172.45.0.1:9305"));

        final EnvElasticSearchConfigurator envConfigurator = new EnvElasticSearchConfigurator(new JsonObject());

        final List<TransportAddress> expected =
                Collections.singletonList(new InetSocketTransportAddress(new InetSocketAddress("172.45.0.1", 9305)));

        final List<TransportAddress> actual = envConfigurator.transportAddresses;

        assertEquals(expected, actual);
    }

    @Test
    public void testInitTransportAddressesWhenMultipleNoPort() throws Exception {
        setEnv(Collections.singletonMap(ENV_TRANSPORT_ADDRESS, "172.45.0.1|172.45.0.2"));

        final EnvElasticSearchConfigurator envConfigurator = new EnvElasticSearchConfigurator(new JsonObject());

        final List<TransportAddress> expected = new LinkedList<>();
        expected.add(new InetSocketTransportAddress(new InetSocketAddress("172.45.0.1", 9300)));
        expected.add(new InetSocketTransportAddress(new InetSocketAddress("172.45.0.2", 9300)));

        final List<TransportAddress> actual = envConfigurator.transportAddresses;

        assertEquals(expected, actual);
    }

    @Test
    public void testInitTransportAddressesWhenMultipleHasPort() throws Exception {
        setEnv(Collections.singletonMap(ENV_TRANSPORT_ADDRESS, "172.45.0.1:9305|172.45.0.2:9306"));

        final EnvElasticSearchConfigurator envConfigurator = new EnvElasticSearchConfigurator(new JsonObject());

        final List<TransportAddress> expected = new LinkedList<>();
        expected.add(new InetSocketTransportAddress(new InetSocketAddress("172.45.0.1", 9305)));
        expected.add(new InetSocketTransportAddress(new InetSocketAddress("172.45.0.2", 9306)));

        final List<TransportAddress> actual = envConfigurator.transportAddresses;

        assertEquals(expected, actual);
    }

    /**
     * Utility method to modify the Environment variables (System.getenv) - {@see https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java}
     * @param newenv the new list of environment variables/values
     * @throws Exception if it fails
     */
    @SuppressWarnings("unchecked")
    private void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }
}
