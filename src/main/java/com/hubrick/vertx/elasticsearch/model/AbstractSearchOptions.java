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

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Search operation options base class
 */
public abstract class AbstractSearchOptions<T extends AbstractSearchOptions<T>> {

    private List<String> types = new ArrayList<>();
    private SearchType searchType;
    private String scroll;
    private Long timeoutInMillis;
    private Integer terminateAfter;
    private String routing;
    private String preference;
    private JsonObject query;
    private JsonObject postFilter;
    private Float minScore;
    private Integer size;
    private Integer from;
    private Boolean explain;
    private Boolean version;
    private Boolean fetchSource;
    private List<String> sourceIncludes = new ArrayList<>();
    private List<String> sourceExcludes = new ArrayList<>();
    private JsonArray searchAfter = new JsonArray();
    private Boolean trackScores;
    private List<AggregationOption> aggregations = new ArrayList<>();
    private List<BaseSortOption> sorts = new ArrayList<>();
    private Map<String, ScriptFieldOption> scriptFields = new HashMap<>();
    private List<String> storedFields = new ArrayList<>();
    private IndicesOptions indicesOptions;
    private Map<String, BaseSuggestOption> suggestions = new HashMap<>();

    public static final String JSON_FIELD_TYPES = "types";
    public static final String JSON_FIELD_SEARCH_TYPE = "searchType";
    public static final String JSON_FIELD_SCROLL = "scroll";
    public static final String JSON_FIELD_TIMEOUT_IN_MILLIS = "timeoutInMillis";
    public static final String JSON_FIELD_TERMINATE_AFTER = "terminateAfter";
    public static final String JSON_FIELD_ROUTING = "routing";
    public static final String JSON_FIELD_PREFERENCE = "preference";
    public static final String JSON_FIELD_QUERY = "query";
    public static final String JSON_FIELD_POST_FILTER = "postFilter";
    public static final String JSON_FIELD_MIN_SCORE = "minScore";
    public static final String JSON_FIELD_SIZE = "size";
    public static final String JSON_FIELD_FROM = "from";
    public static final String JSON_FIELD_EXPLAIN = "explain";
    public static final String JSON_FIELD_VERSION = "version";
    public static final String JSON_FIELD_FETCH_SOURCE = "fetchSource";
    public static final String JSON_FIELD_SOURCE_INCLUDES = "sourceIncludes";
    public static final String JSON_FIELD_SOURCE_EXCLUDES = "sourceExcludes";
    public static final String JSON_FIELD_TRACK_SCORES = "trackScores";
    public static final String JSON_FIELD_AGGREGATIONS = "aggregations";
    public static final String JSON_FIELD_SORTS = "sorts";
    public static final String JSON_FIELD_SCRIPT_FIELDS = "scriptFields";
    public static final String JSON_FIELD_STORED_FIELDS = "storedFields";
    public static final String JSON_FIELD_INDICES_OPTIONS = "indicesOptions";
    public static final String JSON_FIELD_SUGGESTIONS = "suggestions";
    public static final String JSON_FIELD_SEARCH_AFTER = "searchAfter";

    public AbstractSearchOptions() {
    }

    public AbstractSearchOptions(AbstractSearchOptions other) {
        types = other.getTypes();
        searchType = other.getSearchType();
        scroll = other.getScroll();
        timeoutInMillis = other.getTimeoutInMillis();
        terminateAfter = other.getTerminateAfter();
        routing = other.getRouting();
        preference = other.getPreference();
        query = other.getQuery();
        postFilter = other.getPostFilter();
        minScore = other.getMinScore();
        size = other.getSize();
        from = other.getFrom();
        explain = other.isExplain();
        version = other.isVersion();
        fetchSource = other.isFetchSource();
        sourceIncludes = other.getSourceIncludes();
        sourceExcludes = other.getSourceExcludes();
        trackScores = other.isTrackScores();
        aggregations = other.getAggregations();
        sorts = other.getSorts();
        scriptFields = other.getScriptFields();
        storedFields = other.getStoredFields();
        indicesOptions = other.getIndicesOptions();
        suggestions = other.getSuggestions();
        searchAfter = other.getSearchAfter();
    }

    public AbstractSearchOptions(JsonObject json) {

        types = json.getJsonArray(JSON_FIELD_TYPES, new JsonArray()).getList();
        scroll = json.getString(JSON_FIELD_SCROLL);
        timeoutInMillis = json.getLong(JSON_FIELD_TIMEOUT_IN_MILLIS);
        terminateAfter = json.getInteger(JSON_FIELD_TERMINATE_AFTER);
        routing = json.getString(JSON_FIELD_ROUTING);
        preference = json.getString(JSON_FIELD_PREFERENCE);
        query = json.getJsonObject(JSON_FIELD_QUERY);
        postFilter = json.getJsonObject(JSON_FIELD_POST_FILTER);
        minScore = json.getFloat(JSON_FIELD_MIN_SCORE);
        size = json.getInteger(JSON_FIELD_SIZE);
        from = json.getInteger(JSON_FIELD_FROM);
        explain = json.getBoolean(JSON_FIELD_EXPLAIN);
        version = json.getBoolean(JSON_FIELD_VERSION);
        fetchSource = json.getBoolean(JSON_FIELD_FETCH_SOURCE);
        sourceIncludes = json.getJsonArray(JSON_FIELD_SOURCE_INCLUDES, new JsonArray()).getList();
        sourceExcludes = json.getJsonArray(JSON_FIELD_SOURCE_EXCLUDES, new JsonArray()).getList();
        trackScores = json.getBoolean(JSON_FIELD_TRACK_SCORES);
        storedFields = json.getJsonArray(JSON_FIELD_STORED_FIELDS, new JsonArray()).getList();
        indicesOptions = Optional.ofNullable(json.getJsonObject(JSON_FIELD_INDICES_OPTIONS)).map(IndicesOptions::new).orElse(null);
        searchAfter = json.getJsonArray(JSON_FIELD_SEARCH_AFTER);

        JsonArray aggregationsJson = json.getJsonArray(JSON_FIELD_AGGREGATIONS);
        if (aggregationsJson != null) {
            for (int i = 0; i < aggregationsJson.size(); i++) {
                aggregations.add(new AggregationOption(aggregationsJson.getJsonObject(i)));
            }
        }

        searchType = Optional.ofNullable(json.getString(JSON_FIELD_SEARCH_TYPE)).map(SearchType::valueOf).orElse(null);
        JsonArray fieldSortOptionsJson = json.getJsonArray(JSON_FIELD_SORTS);
        if (fieldSortOptionsJson != null) {
            for (int i = 0; i < fieldSortOptionsJson.size(); i++) {
                sorts.add(BaseSortOption.parseJson(fieldSortOptionsJson.getJsonObject(i)));
            }
        }

        JsonObject scriptFieldOptionsJson = json.getJsonObject(JSON_FIELD_SCRIPT_FIELDS);
        if (scriptFieldOptionsJson != null) {
            for (String scriptFieldName : scriptFieldOptionsJson.fieldNames()) {
                scriptFields.put(scriptFieldName, new ScriptFieldOption(scriptFieldOptionsJson.getJsonObject(scriptFieldName)));
            }
        }

        final JsonObject suggestOptionsJson = json.getJsonObject(JSON_FIELD_SUGGESTIONS);
        if (suggestOptionsJson != null) {
            for (String suggestionName : suggestOptionsJson.fieldNames()) {
                suggestions.put(suggestionName, BaseSuggestOption.parseJson(suggestOptionsJson.getJsonObject(suggestionName)));
            }
        }
    }

    public List<String> getTypes() {
        return types;
    }

    public T addType(String type) {
        types.add(type);
        return returnThis();
    }

    public JsonObject getQuery() {
        return query;
    }

    public T setQuery(JsonObject query) {
        this.query = query;
        return returnThis();
    }

    public JsonObject getPostFilter() {
        return postFilter;
    }

    public T setPostFilter(JsonObject postFilter) {
        this.postFilter = postFilter;
        return returnThis();
    }

    public List<AggregationOption> getAggregations() {
        return aggregations;
    }

    public T setAggregations(List<AggregationOption> aggregations) {
        this.aggregations = aggregations;
        return returnThis();
    }

    @GenIgnore
    public T addAggregation(AggregationOption aggregation) {
        if (this.aggregations == null) {
            this.aggregations = new ArrayList<>();
        }
        this.aggregations.add(aggregation);
        return returnThis();
    }


    public SearchType getSearchType() {
        return searchType;
    }

    public T setSearchType(SearchType searchType) {
        this.searchType = searchType;
        return returnThis();
    }

    public String getScroll() {
        return scroll;
    }

    public T setScroll(String keepAlive) {
        this.scroll = keepAlive;
        return returnThis();
    }

    public Integer getSize() {
        return size;
    }

    public T setSize(Integer size) {
        this.size = size;
        return returnThis();
    }

    public Integer getFrom() {
        return from;
    }

    public T setFrom(Integer from) {
        this.from = from;
        return returnThis();
    }

    public Long getTimeoutInMillis() {
        return timeoutInMillis;
    }

    public T setTimeoutInMillis(Long timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
        return returnThis();
    }

    public List<String> getSourceIncludes() {
        return sourceIncludes;
    }

    public T setSourceIncludes(List<String> sourceIncludes) {
        this.sourceIncludes = sourceIncludes;
        return returnThis();
    }

    public List<String> getSourceExcludes() {
        return sourceExcludes;
    }

    public T setSourceExcludes(List<String> sourceExcludes) {
        this.sourceExcludes = sourceExcludes;
        return returnThis();
    }

    public List<BaseSortOption> getSorts() {
        return sorts;
    }

    @GenIgnore
    public T addFieldSort(String field, SortOrder order) {
        sorts.add(new FieldSortOption().setField(field).setOrder(order));
        return returnThis();
    }

    @GenIgnore
    public T addScripSort(String script, ScriptSortOption.Type type, JsonObject params, SortOrder order) {
        sorts.add(new ScriptSortOption().setScript(script).setType(type).setParams(params).setOrder(order));
        return returnThis();
    }

    @GenIgnore
    public T addScripSort(String script, String lang, ScriptSortOption.Type type, JsonObject params, SortOrder order) {
        sorts.add(new ScriptSortOption().setScript(script).setLang(lang).setType(type).setParams(params).setOrder(order));
        return returnThis();
    }

    public Integer getTerminateAfter() {
        return terminateAfter;
    }

    public T setTerminateAfter(Integer terminateAfter) {
        this.terminateAfter = terminateAfter;
        return returnThis();
    }

    public String getRouting() {
        return routing;
    }

    public T setRouting(String routing) {
        this.routing = routing;
        return returnThis();
    }

    public String getPreference() {
        return preference;
    }

    public T setPreference(String preference) {
        this.preference = preference;
        return returnThis();
    }

    public Float getMinScore() {
        return minScore;
    }

    public T setMinScore(Float minScore) {
        this.minScore = minScore;
        return returnThis();
    }

    public Boolean isExplain() {
        return explain;
    }

    public T setExplain(Boolean explain) {
        this.explain = explain;
        return returnThis();
    }

    public Boolean isVersion() {
        return version;
    }

    public T setVersion(Boolean version) {
        this.version = version;
        return returnThis();
    }

    public Boolean isFetchSource() {
        return fetchSource;
    }

    public T setFetchSource(Boolean fetchSource) {
        this.fetchSource = fetchSource;
        return returnThis();
    }

    public Boolean isTrackScores() {
        return trackScores;
    }

    public T setTrackScores(Boolean trackScores) {
        this.trackScores = trackScores;
        return returnThis();
    }

    public Map<String, ScriptFieldOption> getScriptFields() {
        return scriptFields;
    }

    @GenIgnore
    public T addScriptField(String name, String script, JsonObject params) {
        scriptFields.put(name, new ScriptFieldOption().setScript(script).setParams(params));
        return returnThis();
    }

    @GenIgnore
    public T addScriptField(String name, String script, String lang, JsonObject params) {
        scriptFields.put(name, new ScriptFieldOption().setScript(script).setLang(lang).setParams(params));
        return returnThis();
    }

    public List<String> getStoredFields() {
        return storedFields;
    }

    public void setStoredFields(List<String> storedFields) {
        this.storedFields = storedFields;
    }

    @GenIgnore
    public T addStoredField(String storedField) {
        if (this.storedFields == null) {
            this.storedFields = new ArrayList<>();
        }
        this.storedFields.add(storedField);
        return returnThis();
    }

    public IndicesOptions getIndicesOptions() {
        return indicesOptions;
    }

    public T setIndicesOptions(IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
        return returnThis();
    }

    public Map<String, BaseSuggestOption> getSuggestions() {
        return suggestions;
    }

    @GenIgnore
    public T addSuggestion(String name, BaseSuggestOption baseSuggestOption) {
        suggestions.put(name, baseSuggestOption);
        return returnThis();
    }

    public JsonArray getSearchAfter() {
        return searchAfter;
    }

    public T setSearchAfter(JsonArray searchAfter) {
        this.searchAfter = searchAfter;
        return returnThis();
    }

    @GenIgnore
    public boolean hasSearchAfter() {
        return this.searchAfter != null && !this.searchAfter.isEmpty();
    }
    
    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (!types.isEmpty()) json.put(JSON_FIELD_TYPES, new JsonArray(types));
        if (searchType != null) json.put(JSON_FIELD_SEARCH_TYPE, searchType.name());
        if (scroll != null) json.put(JSON_FIELD_SCROLL, scroll);
        if (timeoutInMillis != null) json.put(JSON_FIELD_TIMEOUT_IN_MILLIS, timeoutInMillis);
        if (terminateAfter != null) json.put(JSON_FIELD_TERMINATE_AFTER, terminateAfter);
        if (routing != null) json.put(JSON_FIELD_ROUTING, routing);
        if (preference != null) json.put(JSON_FIELD_PREFERENCE, preference);
        if (query != null) json.put(JSON_FIELD_QUERY, query);
        if (postFilter != null) json.put(JSON_FIELD_POST_FILTER, postFilter);
        if (minScore != null) json.put(JSON_FIELD_MIN_SCORE, minScore);
        if (size != null) json.put(JSON_FIELD_SIZE, size);
        if (from != null) json.put(JSON_FIELD_FROM, from);
        if (explain != null) json.put(JSON_FIELD_EXPLAIN, explain);
        if (version != null) json.put(JSON_FIELD_VERSION, version);
        if (fetchSource != null) json.put(JSON_FIELD_FETCH_SOURCE, fetchSource);
        if (!sourceIncludes.isEmpty()) json.put(JSON_FIELD_SOURCE_INCLUDES, new JsonArray(sourceIncludes));
        if (!sourceExcludes.isEmpty()) json.put(JSON_FIELD_SOURCE_EXCLUDES, new JsonArray(sourceExcludes));
        if (trackScores != null) json.put(JSON_FIELD_TRACK_SCORES, trackScores);
        if (explain != null) json.put(JSON_FIELD_EXPLAIN, explain);
        if (!storedFields.isEmpty()) json.put(JSON_FIELD_STORED_FIELDS, new JsonArray(storedFields));
        if (indicesOptions != null) json.put(JSON_FIELD_INDICES_OPTIONS, indicesOptions.toJson());

        if (aggregations != null && !aggregations.isEmpty()) {
            JsonArray aggregationArray = new JsonArray();
            for (AggregationOption option : aggregations) {
                aggregationArray.add(option.toJson());
            }
            json.put(JSON_FIELD_AGGREGATIONS, aggregationArray);
        }

        if (!sorts.isEmpty()) {
            JsonArray jsonSorts = new JsonArray();
            sorts.forEach(sort -> jsonSorts.add(sort.toJson()));
            json.put(JSON_FIELD_SORTS, jsonSorts);
        }

        if (!scriptFields.isEmpty()) {
            JsonObject scriptFieldsJson = new JsonObject();
            for (Map.Entry<String, ScriptFieldOption> scriptFieldOptionEntry : scriptFields.entrySet()) {
                scriptFieldsJson.put(scriptFieldOptionEntry.getKey(), scriptFieldOptionEntry.getValue().toJson());
            }
            json.put(JSON_FIELD_SCRIPT_FIELDS, scriptFieldsJson);
        }

        if (!suggestions.isEmpty()) {
            final JsonObject jsonSuggestions = new JsonObject();
            suggestions.entrySet().forEach(suggestion -> jsonSuggestions.put(suggestion.getKey(), suggestion.getValue().toJson()));
            json.put(JSON_FIELD_SUGGESTIONS, jsonSuggestions);
        }

        if (this.hasSearchAfter()) {
            json.put(JSON_FIELD_SEARCH_AFTER, searchAfter);
        }
        
        return json;
    }

    @SuppressWarnings("unchecked")
    protected T returnThis() {
        return (T) this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
