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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Get operation options
 */
@DataObject
public class GetOptions extends AbstractOptions<GetOptions> {

    private String preference;
    private List<String> fields = new ArrayList<>();
    private Boolean fetchSource;
    private List<String> fetchSourceIncludes = new ArrayList<>();
    private List<String> fetchSourceExcludes = new ArrayList<>();
    private Boolean realtime;

    public static final String FIELD_PREFERENCE = "preference";
    public static final String FIELD_FIELDS = "fields";
    public static final String FIELD_FETCH_SOURCE = "fetchSource";
    public static final String FIELD_FETCH_SOURCE_INCLUDES = "fetchSourceIncludes";
    public static final String FIELD_FETCH_SOURCE_EXCLUDES = "fetchSourceExcludes";
    public static final String FIELD_REALTIME = "realtime";

    public GetOptions() {
    }

    public GetOptions(GetOptions other) {
        super(other);

        preference = other.getPreference();
        fields.addAll(other.getFields());
        fetchSource = other.isFetchSource();
        fetchSourceIncludes = other.getFetchSourceIncludes();
        fetchSourceExcludes = other.getFetchSourceExcludes();
        realtime = other.isRealtime();
    }

    public GetOptions(JsonObject json) {
        super(json);

        preference= json.getString(FIELD_PREFERENCE);
        fields = json.getJsonArray(FIELD_FIELDS, new JsonArray()).getList();
        fetchSource = json.getBoolean(FIELD_FETCH_SOURCE);
        fetchSourceIncludes = json.getJsonArray(FIELD_FETCH_SOURCE_INCLUDES, new JsonArray()).getList();
        fetchSourceExcludes = json.getJsonArray(FIELD_FETCH_SOURCE_EXCLUDES, new JsonArray()).getList();
        realtime = json.getBoolean(FIELD_REALTIME);

    }

    public String getPreference() {
        return preference;
    }

    public GetOptions setPreference(String preference) {
        this.preference = preference;
        return this;
    }

    public List<String> getFields() {
        return fields;
    }

    public GetOptions addField(String field) {
        this.fields.add(field);
        return this;
    }

    public Boolean isFetchSource() {
        return fetchSource;
    }

    public GetOptions setFetchSource(Boolean fetchSource) {
        this.fetchSource = fetchSource;
        return this;
    }

    public List<String> getFetchSourceIncludes() {
        return fetchSourceIncludes;
    }

    public List<String> getFetchSourceExcludes() {
        return fetchSourceExcludes;
    }

    public GetOptions setFetchSource(List<String> includes, List<String> excludes) {
        if (includes == null || includes.isEmpty()) {
            fetchSourceIncludes.clear();
        } else {
            fetchSourceIncludes.addAll(includes);
        }
        if (excludes == null || excludes.isEmpty()) {
            fetchSourceExcludes.clear();
        } else {
            fetchSourceExcludes.addAll(excludes);
        }
        return this;
    }

    public Boolean isRealtime() {
        return realtime;
    }

    public GetOptions setRealtime(Boolean realtime) {
        this.realtime = realtime;
        return this;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();

        if (getPreference() != null) json.put(FIELD_PREFERENCE, getPreference());
        if (!getFields().isEmpty()) json.put(FIELD_FIELDS, new JsonArray(getFields()));
        if (isFetchSource() != null) json.put(FIELD_FETCH_SOURCE, isFetchSource());
        if (!getFetchSourceIncludes().isEmpty()) json.put(FIELD_FETCH_SOURCE_INCLUDES, new JsonArray(getFetchSourceIncludes()));
        if (!getFetchSourceExcludes().isEmpty()) json.put(FIELD_FETCH_SOURCE_EXCLUDES, new JsonArray(getFetchSourceExcludes()));
        if (isRealtime() != null) json.put(FIELD_REALTIME, isRealtime());

        return json;
    }
}
