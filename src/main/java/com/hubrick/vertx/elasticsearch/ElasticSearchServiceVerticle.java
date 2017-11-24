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
package com.hubrick.vertx.elasticsearch;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

import javax.inject.Inject;

/**
 * ElasticSearch event bus service verticle
 */
public class ElasticSearchServiceVerticle extends AbstractVerticle {

    private final ElasticSearchService service;
    private final ElasticSearchAdminService adminService;

    @Inject
    public ElasticSearchServiceVerticle(ElasticSearchService service, ElasticSearchAdminService adminService) {
        this.service = service;
        this.adminService = adminService;
    }

    @Override
    public void start() throws Exception {

        // workaround for problem between ES netty and vertx (both wanting to set the same value)
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        String address = config().getString("address");
        if (address == null || address.isEmpty()) {
            throw new IllegalStateException("address field must be specified in config for service verticle");
        }
        String adminAddress = config().getString("address.admin");
        if (adminAddress == null || adminAddress.isEmpty()) {
            adminAddress = address + ".admin";
        }

        // Register service as an event bus proxy
        new ServiceBinder(vertx).setAddress(address).register(ElasticSearchService.class, service);
        new ServiceBinder(vertx).setAddress(adminAddress).register(ElasticSearchAdminService.class, adminService);

        // Start the service
        service.start();

    }

    @Override
    public void stop() throws Exception {
        service.stop();
    }

}
