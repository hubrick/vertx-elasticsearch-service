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

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregation option
 */
@DataObject
public class AggregationOption {
    private String name;
    private AggregationType type;
    private JsonObject definition;
    private List<AggregationOption> subAggregations = new ArrayList<>();

    private static final String JSON_FIELD_NAME = "name";
    private static final String JSON_FIELD_TYPE = "type";
    private static final String JSON_FIELD_DEFINITION = "definition";
    private static final String JSON_FIELD_SUB_AGGREGATIONS = "subAggregations";

    public AggregationOption() {
    }

    public AggregationOption(AggregationOption other) {
        name = other.getName();
        type = other.getType();
        definition = other.getDefinition();
        subAggregations = other.getSubAggregations();
    }

    public AggregationOption(JsonObject json) {
        name = json.getString(JSON_FIELD_NAME);
        type = AggregationType.valueOf(json.getString(JSON_FIELD_TYPE));
        definition = json.getJsonObject(JSON_FIELD_DEFINITION);

        JsonArray aggregationsJson = json.getJsonArray(JSON_FIELD_SUB_AGGREGATIONS);
        if (aggregationsJson != null) {
            for (int i = 0; i < aggregationsJson.size(); i++) {
                subAggregations.add(new AggregationOption(aggregationsJson.getJsonObject(i)));
            }
        }
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

    public List<AggregationOption> getSubAggregations() {
        return subAggregations;
    }

    public AggregationOption setSubAggregations(List<AggregationOption> subAggregations) {
        this.subAggregations = subAggregations;
        return this;
    }

    @GenIgnore
    public AggregationOption addSubAggregation(AggregationOption subAggregation) {
        this.subAggregations.add(subAggregation);
        return this;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put(JSON_FIELD_NAME, name)
                .put(JSON_FIELD_TYPE, type)
                .put(JSON_FIELD_DEFINITION, definition);

        if (subAggregations != null && !subAggregations.isEmpty()) {
            JsonArray aggregationArray = new JsonArray();
            for (AggregationOption option : subAggregations) {
                aggregationArray.add(option.toJson());
            }
            json.put(JSON_FIELD_SUB_AGGREGATIONS, aggregationArray);
        }
        return json;
    }
}
