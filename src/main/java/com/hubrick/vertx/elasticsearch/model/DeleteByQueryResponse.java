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
package com.hubrick.vertx.elasticsearch.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@DataObject
public class DeleteByQueryResponse extends AbstractRawResponse<DeleteByQueryResponse> {

    private Long took; // in ms
    private Boolean timedOut;
    private Long deleted;
    private Integer batches;
    private Long versionConflicts;
    private Long retries;
    private Long throttled; // in ms
    private JsonArray failures = new JsonArray();


    public static final String JSON_FIELD_TOOK = "took";
    public static final String JSON_FIELD_TIMED_OUT = "timedOut";
    public static final String JSON_FIELD_DELETED = "deleted";
    public static final String JSON_FIELD_BATCHES = "batches";
    public static final String JSON_FIELD_VERSION_CONFLICTS = "versionConflicts";
    public static final String JSON_FIELD_RETRIES = "retries";
    public static final String JSON_FIELD_THROTTLED = "throttled";
    public static final String JSON_FIELD_FAILURES = "failures";

    public DeleteByQueryResponse() {
    }

    public DeleteByQueryResponse(DeleteByQueryResponse other) {
        super(other);

        this.took = other.getTook();
        this.timedOut = other.getTimedOut();
        this.deleted = other.getDeleted();
        this.batches = other.getBatches();
        this.versionConflicts = other.getVersionConflicts();
        this.retries = other.getRetries();
        this.throttled = other.getThrottled();
        this.failures = other.getFailures();

    }

    public DeleteByQueryResponse(JsonObject json) {
        super(json);

        this.took = json.getLong(JSON_FIELD_TOOK);
        this.timedOut = json.getBoolean(JSON_FIELD_TIMED_OUT);
        this.deleted = json.getLong(JSON_FIELD_DELETED);
        this.batches = json.getInteger(JSON_FIELD_BATCHES);
        this.versionConflicts = json.getLong(JSON_FIELD_VERSION_CONFLICTS);
        this.retries = json.getLong(JSON_FIELD_RETRIES);
        this.throttled = json.getLong(JSON_FIELD_THROTTLED);
        this.failures = json.getJsonArray(JSON_FIELD_FAILURES);
    }

    public Long getTook() {
        return took;
    }

    public DeleteByQueryResponse setTook(Long took) {
        this.took = took;
        return this;
    }

    public Boolean getTimedOut() {
        return timedOut;
    }

    public DeleteByQueryResponse setTimedOut(Boolean timedOut) {
        this.timedOut = timedOut;
        return this;
    }

    public Long getDeleted() {
        return deleted;
    }

    public DeleteByQueryResponse setDeleted(Long deleted) {
        this.deleted = deleted;
        return this;
    }

    public Integer getBatches() {
        return batches;
    }

    public DeleteByQueryResponse setBatches(Integer batches) {
        this.batches = batches;
        return this;
    }

    public Long getVersionConflicts() {
        return versionConflicts;
    }

    public DeleteByQueryResponse setVersionConflicts(Long versionConflicts) {
        this.versionConflicts = versionConflicts;
        return this;
    }

    public Long getRetries() {
        return retries;
    }

    public DeleteByQueryResponse setRetries(Long retries) {
        this.retries = retries;
        return this;
    }

    public Long getThrottled() {
        return throttled;
    }

    public DeleteByQueryResponse setThrottled(Long throttled) {
        this.throttled = throttled;
        return this;
    }

    public JsonArray getFailures() {
        return failures;
    }

    @GenIgnore
    public DeleteByQueryResponse addFailure(JsonObject failure) {
        this.failures.add(failure);
        return this;
    }

    public DeleteByQueryResponse setFailures(JsonArray failures) {
        this.failures = failures;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (took != null) json.put(JSON_FIELD_TOOK, took);
        if (timedOut != null) json.put(JSON_FIELD_TIMED_OUT, timedOut);
        if (deleted != null) json.put(JSON_FIELD_DELETED, deleted);
        if (batches != null) json.put(JSON_FIELD_BATCHES, batches);
        if (versionConflicts != null) json.put(JSON_FIELD_VERSION_CONFLICTS, versionConflicts);
        if (retries != null) json.put(JSON_FIELD_RETRIES, retries);
        if (throttled != null) json.put(JSON_FIELD_THROTTLED, throttled);
        if (failures != null) json.put(JSON_FIELD_FAILURES, failures);

        return json.mergeIn(super.toJson());
    }
}
