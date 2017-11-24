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

import com.hubrick.vertx.elasticsearch.ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.model.MappingOptions;
import com.hubrick.vertx.elasticsearch.internal.InternalElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.internal.InternalElasticSearchService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.AdminClient;

import javax.inject.Inject;
import java.util.List;

/**
 * Default implementation of {@link ElasticSearchAdminService}
 */
public class DefaultElasticSearchAdminService implements InternalElasticSearchAdminService {

    private final InternalElasticSearchService service;

    @Inject
    public DefaultElasticSearchAdminService(InternalElasticSearchService service) {
        this.service = service;
    }

    @Override
    public void putMapping(List<String> indices, String type, JsonObject source, MappingOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        PutMappingRequestBuilder builder = getAdmin().indices()
                .preparePutMapping(indices.toArray(new String[indices.size()]))
                .setType(type)
                .setSource(source.encode());

        // TODO: PutMappingRequestBuilder setIgnoreConflicts() was removed in ES 2.0.0
//        if (options != null) {
//            if (options.shouldIgnoreConflicts() != null) builder.setIgnoreConflicts(options.shouldIgnoreConflicts());
//        }

        builder.execute(new ActionListener<PutMappingResponse>() {
            @Override
            public void onResponse(PutMappingResponse putMappingResponse) {
                JsonObject json = new JsonObject()
                        .put("acknowledged", putMappingResponse.isAcknowledged());
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Exception e) {
                resultHandler.handle(Future.failedFuture(e));
            }
        });

    }

    /**
     * Returns the inner admin client
     *
     * @return
     */
    @Override
    public AdminClient getAdmin() {
        return service.getClient().admin();
    }

}
