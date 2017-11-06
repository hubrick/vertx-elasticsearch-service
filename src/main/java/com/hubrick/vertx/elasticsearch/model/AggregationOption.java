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
import io.vertx.core.json.JsonObject;

/**
 * Aggregation option
 */
@DataObject
public class AggregationOption {
    private String name;
    private AggregationType type;
    private JsonObject definition;

    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_DEFINITION = "definition";

    public AggregationOption() {
    }

    public AggregationOption(AggregationOption other) {
        name = other.getName();
        type = other.getType();
        definition = other.getDefinition();
    }

    public AggregationOption(JsonObject json) {
        name = json.getString(JSON_FIELD_NAME);
        type = AggregationType.valueOf(json.getString(JSON_FIELD_TYPE));
        definition = json.getJsonObject(JSON_FIELD_DEFINITION);
    }

    public enum AggregationType {
        VALUE_COUNT, AVERAGE, MAX, MIN, SUM, STATS, EXTENDED_STATS,
        FILTER, FILTERS, ADJACENCY_MATRIX, SAMPLER, DIVERSIFIED_SAMPLER,
        GLOBAL, MISSING, NESTED, REVERSE_NESTED, GEO_DISTANCE, HISTOGRAM,
        GEO_GRID, DATE_HISTOGRAM, RANGE, DATE_RANGE,
        IP_RANGE, TERMS, PERCENTILES, PERCENTILE_RANKS, CARDINALITY,
        TOP_HITS, GEO_BOUNDS, GEO_CENTROID, SCRIPTED_METRIC
    }

    public String getName() {
        return name;
    }

    public AggregationOption setName(String name) {
        this.name = name;
        return this;
    }

    public AggregationType getType() {
        return type;
    }

    public AggregationOption setType(AggregationType type) {
        this.type = type;
        return this;
    }

    public JsonObject getDefinition() {
        return definition;
    }

    public AggregationOption setDefinition(JsonObject definition) {
        this.definition = definition;
        return this;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(JSON_FIELD_NAME, name)
                .put(JSON_FIELD_TYPE, type)
                .put(JSON_FIELD_DEFINITION, definition);
    }
}
