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

import com.hubrick.vertx.elasticsearch.TransportClientFactory;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * Default implementation of {@link TransportClientFactory}
 */
public class DefaultTransportClientFactory implements TransportClientFactory {
    /**
     * Create a client from the settings
     *
     * @param settings the settings used to create the client
     * @return transport client
     */
    @Override
    public TransportClient create(Settings settings) {
        return new PreBuiltTransportClient(settings);
    }
}
