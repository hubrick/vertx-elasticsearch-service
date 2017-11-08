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

import com.hubrick.vertx.elasticsearch.ElasticSearchConfigurator;
import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.TransportClientFactory;
import com.hubrick.vertx.elasticsearch.internal.InternalElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.geodistance.GeoDistanceAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.ip.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.DiversifiedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBoundsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.geocentroid.GeoCentroidAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentileRanksAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.scripted.ScriptedMetricAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.StatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStatsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.*;

/**
 * Default implementation of {@link ElasticSearchService}
 */
public class DefaultElasticSearchService implements InternalElasticSearchService {

    private final Logger log = LoggerFactory.getLogger(DefaultElasticSearchService.class);
    private final TransportClientFactory clientFactory;
    private final ElasticSearchConfigurator configurator;
    protected TransportClient client;

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    @Inject
    public DefaultElasticSearchService(TransportClientFactory clientFactory, ElasticSearchConfigurator configurator) {
        this.clientFactory = clientFactory;
        this.configurator = configurator;
    }

    @Override
    public void start() {

        Settings settings = Settings.builder()
                .put("cluster.name", configurator.getClusterName())
                .put("client.transport.sniff", configurator.getClientTransportSniff())
                .build();

        client = clientFactory.create(settings);
        configurator.getTransportAddresses().forEach(client::addTransportAddress);

    }

    @Override
    public void stop() {
        client.close();
        client = null;
    }

    @Override
    public void index(String index, String type, JsonObject source, IndexOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.IndexResponse>> resultHandler) {

        IndexRequestBuilder builder = client.prepareIndex(index, type)
                .setSource(source.encode());

        if (options != null) {
            if (options.getId() != null) builder.setId(options.getId());
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.getOpType() != null) builder.setOpType(options.getOpType());
            if (options.getWaitForActiveShard() != null) builder.setWaitForActiveShards(options.getWaitForActiveShard());
            if (options.isRefresh() != null) builder.setRefreshPolicy(options.isRefresh() ? WriteRequest.RefreshPolicy.IMMEDIATE : WriteRequest.RefreshPolicy.NONE);
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimestamp() != null) builder.setTimestamp(options.getTimestamp());
            if (options.getTtl() != null) builder.setTTL(options.getTtl());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());
        }

        builder.execute(new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                resultHandler.handle(Future.succeededFuture(mapToIndexResponse(indexResponse)));
            }

            @Override
            public void onFailure(Exception e) {
                handleFailure(resultHandler, e);
            }
        });

    }

    @Override
    public void update(String index, String type, String id, UpdateOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.UpdateResponse>> resultHandler) {

        UpdateRequestBuilder builder = client.prepareUpdate(index, type, id);

        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.isRefresh() != null) builder.setRefreshPolicy(options.isRefresh() ? WriteRequest.RefreshPolicy.IMMEDIATE : WriteRequest.RefreshPolicy.NONE);
            if (options.getWaitForActiveShard() != null) builder.setWaitForActiveShards(options.getWaitForActiveShard());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());

            if (options.getRetryOnConflict() != null) builder.setRetryOnConflict(options.getRetryOnConflict());
            if (options.getDoc() != null) builder.setDoc(options.getDoc().encode());
            if (options.getUpsert() != null) builder.setUpsert(options.getUpsert().encode());
            if (options.isDocAsUpsert() != null) builder.setDocAsUpsert(options.isDocAsUpsert());
            if (options.isDetectNoop() != null) builder.setDetectNoop(options.isDetectNoop());
            if (options.isScriptedUpsert() != null) builder.setScriptedUpsert(options.isScriptedUpsert());

            if (options.getScript() != null) {
                if (options.getScriptType() != null) {
                    Map<String, Object> params = (options.getScriptParams() == null ? null : convertJsonObjectToMap(options.getScriptParams()));
                    builder.setScript(new Script(options.getScriptType(), options.getScriptLang(), options.getScript(), params));
                } else {
                    builder.setScript(new Script(options.getScript()));
                }
            }
            if (!options.getFields().isEmpty()) {
                builder.setFields(options.getFields().toArray(new String[options.getFields().size()]));
            }
        }

        builder.execute(new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                resultHandler.handle(Future.succeededFuture(mapToUpdateResponse(updateResponse)));
            }

            @Override
            public void onFailure(Exception e) {
                handleFailure(resultHandler, e);
            }
        });

    }

    @Override
    public void get(String index, String type, String id, GetOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.GetResponse>> resultHandler) {

        GetRequestBuilder builder = client.prepareGet(index, type, id);

        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.isRefresh() != null) builder.setRefresh(options.isRefresh());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());

            if (options.getPreference() != null) builder.setPreference(options.getPreference());
            if (!options.getFields().isEmpty()) {
                builder.setStoredFields(options.getFields().toArray(new String[options.getFields().size()]));
            }
            if (options.isFetchSource() != null) builder.setFetchSource(options.isFetchSource());
            if (!options.getFetchSourceIncludes().isEmpty() || !options.getFetchSourceExcludes().isEmpty()) {
                String[] includes = options.getFetchSourceIncludes().toArray(new String[options.getFetchSourceIncludes().size()]);
                String[] excludes = options.getFetchSourceExcludes().toArray(new String[options.getFetchSourceExcludes().size()]);
                builder.setFetchSource(includes, excludes);
            }
            if (options.isRealtime() != null) builder.setRealtime(options.isRealtime());
        }

        builder.execute(new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {
                resultHandler.handle(Future.succeededFuture(mapToUpdateResponse(getResponse)));
            }

            @Override
            public void onFailure(Exception t) {
                handleFailure(resultHandler, t);
            }
        });

    }

    @Override
    public void search(List<String> indices, SearchOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.SearchResponse>> resultHandler) {

        SearchRequestBuilder builder = client.prepareSearch(indices.toArray(new String[indices.size()]));

        if (options != null) {
            if (!options.getTypes().isEmpty()) {
                builder.setTypes(options.getTypes().toArray(new String[options.getTypes().size()]));
            }
            if (options.getSearchType() != null) builder.setSearchType(options.getSearchType());
            if (options.getScroll() != null) builder.setScroll(options.getScroll());
            if (options.getTimeout() != null) builder.setTimeout(TimeValue.parseTimeValue(options.getTimeout(), ""));
            if (options.getTerminateAfter() != null) builder.setTerminateAfter(options.getTerminateAfter());
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getPreference() != null) builder.setPreference(options.getPreference());
            if (options.getQuery() != null) builder.setQuery(QueryBuilders.wrapperQuery(options.getQuery().encode()));
            if (options.getPostFilter() != null) builder.setPostFilter(QueryBuilders.wrapperQuery(options.getPostFilter().encode()));
            if (options.getMinScore() != null) builder.setMinScore(options.getMinScore());
            if (options.getSize() != null) builder.setSize(options.getSize());
            if (options.getFrom() != null) builder.setFrom(options.getFrom());
            if (options.isExplain() != null) builder.setExplain(options.isExplain());
            if (options.isVersion() != null) builder.setVersion(options.isVersion());
            if (options.isFetchSource() != null) builder.setFetchSource(options.isFetchSource());
            if (options.isTrackScores() != null) builder.setTrackScores(options.isTrackScores());

            if (!options.getSourceIncludes().isEmpty() || !options.getSourceExcludes().isEmpty()) {
                builder.setFetchSource(
                        options.getSourceIncludes().toArray(new String[options.getSourceIncludes().size()]),
                        options.getSourceExcludes().toArray(new String[options.getSourceExcludes().size()])
                );
            }

            if (options.getAggregations() != null) {
                options.getAggregations().forEach(aggregationOption -> {
                   builder.addAggregation(parseAggregation(aggregationOption));
                });
            }
            if (!options.getSorts().isEmpty()) {
                for (BaseSortOption baseSortOption : options.getSorts()) {
                    switch (baseSortOption.getSortType()) {
                        case FIELD:
                            final FieldSortOption fieldSortOption = (FieldSortOption) baseSortOption;
                            builder.addSort(fieldSortOption.getField(), fieldSortOption.getOrder());
                            break;
                        case SCRIPT:
                            final ScriptSortOption scriptSortOption = (ScriptSortOption) baseSortOption;
                            final Script script = new Script(ScriptType.INLINE, scriptSortOption.getLang(), scriptSortOption.getScript(), convertJsonObjectToMap(scriptSortOption.getParams()));
                            final ScriptSortBuilder scriptSortBuilder = new ScriptSortBuilder(script, ScriptSortBuilder.ScriptSortType.fromString(scriptSortOption.getType().getValue())).order(scriptSortOption.getOrder());
                            builder.addSort(scriptSortBuilder);
                            break;
                    }
                }
            }

            if (!options.getScriptFields().isEmpty()) {
                options.getScriptFields().forEach((scriptName, scriptValue) -> {
                    final Script script = new Script(ScriptType.INLINE, scriptValue.getLang(), scriptValue.getScript(), convertJsonObjectToMap(scriptValue.getParams()));
                    builder.addScriptField(scriptName, script);
                });
            }

            builder.execute(new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    resultHandler.handle(Future.succeededFuture(mapToSearchResponse(searchResponse)));
                }

                @Override
                public void onFailure(Exception t) {
                    handleFailure(resultHandler, t);
                }
            });
        }
    }


    @Override
    public void searchScroll(String scrollId, SearchScrollOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.SearchResponse>> resultHandler) {
        SearchScrollRequestBuilder builder = client.prepareSearchScroll(scrollId);

        if (options != null) {
            if (options.getScroll() != null) builder.setScroll(options.getScroll());
        }

        builder.execute(new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                resultHandler.handle(Future.succeededFuture(mapToSearchResponse(searchResponse)));
            }

            @Override
            public void onFailure(Exception t) {
                handleFailure(resultHandler, t);
            }
        });
    }


    @Override
    public void delete(String index, String type, String id, DeleteOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.DeleteResponse>> resultHandler) {

        DeleteRequestBuilder builder = client.prepareDelete(index, type, id);

        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.isRefresh() != null) builder.setRefreshPolicy(options.isRefresh() ? WriteRequest.RefreshPolicy.IMMEDIATE : WriteRequest.RefreshPolicy.NONE);
            if (options.getWaitForActiveShard() != null) builder.setWaitForActiveShards(options.getWaitForActiveShard());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());
        }

        builder.execute(new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                resultHandler.handle(Future.succeededFuture(mapToDeleteResponse(deleteResponse)));
            }

            @Override
            public void onFailure(Exception t) {
                handleFailure(resultHandler, t);
            }
        });

    }


    @Override
    public void suggest(List<String> indices, SuggestOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.SuggestResponse>> resultHandler) {

        final SearchRequestBuilder builder = client.prepareSearch(indices.toArray(new String[indices.size()]));

        if (options != null && !options.getSuggestions().isEmpty()) {
            for (Map.Entry<String, BaseSuggestOption> suggestOptionEntry : options.getSuggestions().entrySet()) {
                switch (suggestOptionEntry.getValue().getSuggestionType()) {
                    case COMPLETION:
                        final CompletionSuggestOption completionSuggestOption = (CompletionSuggestOption) suggestOptionEntry.getValue();
                        final CompletionSuggestionBuilder completionBuilder = new CompletionSuggestionBuilder(completionSuggestOption.getField());
                        if (completionSuggestOption.getText() != null) {
                            completionBuilder.text(completionSuggestOption.getText());
                        }
                        if (completionSuggestOption.getSize() != null) {
                            completionBuilder.size(completionSuggestOption.getSize());
                        }

                        builder.suggest(new SuggestBuilder().addSuggestion(suggestOptionEntry.getKey() , completionBuilder));
                        break;
                }
            }
        }

        builder.execute(new ActionListener<SearchResponse>() {

            @Override
            public void onResponse(SearchResponse suggestResponse) {
                resultHandler.handle(Future.succeededFuture(mapToSuggestResponse(suggestResponse)));
            }

            @Override
            public void onFailure(Exception t) {
                handleFailure(resultHandler, t);
            }
        });
    }


    @Override
    public void deleteByQuery(List<String> indices, JsonObject query, DeleteByQueryOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse>> resultHandler) {
        final DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE);

        deleteByQueryRequestBuilder.source(indices.toArray(new String[indices.size()]));
        if (query != null) {
            deleteByQueryRequestBuilder.source().setQuery(QueryBuilders.wrapperQuery("{\"query\": " + query.encode() + "}"));
        }

        if (options != null) {
            if (!options.getTypes().isEmpty()) {
                deleteByQueryRequestBuilder.source().setTypes(options.getTypes().toArray(new String[options.getTypes().size()]));
            }
            if (options.getTimeout() != null) deleteByQueryRequestBuilder.source().setTimeout(TimeValue.parseTimeValue(options.getTimeout(), ""));
            if (options.getRouting() != null) deleteByQueryRequestBuilder.source().setRouting(options.getRouting());
        }

        deleteByQueryRequestBuilder.execute(new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse deleteByQueryResponse) {
                resultHandler.handle(Future.succeededFuture(mapToDeleteByQueryResponse(deleteByQueryResponse)));
            }

            @Override
            public void onFailure(Exception t) {
                handleFailure(resultHandler, t);
            }
        });
    }


    @Override
    public TransportClient getClient() {
        return client;
    }

    private <T> void handleFailure(final Handler<AsyncResult<T>> resultHandler, final Throwable t) {
        log.error("Error occurred in ElasticSearchService", t);

        if (t instanceof ElasticsearchException) {
            final ElasticsearchException esException = (ElasticsearchException) t;
            resultHandler.handle(Future.failedFuture(esException.getDetailedMessage()));
        } else {
            resultHandler.handle(Future.failedFuture(t));
        }
    }

    private Map<String, Object> convertJsonObjectToMap(JsonObject jsonObject) {

        final Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> jsonObjectEntry : jsonObject) {
            if (jsonObjectEntry.getValue() instanceof JsonArray) {
                map.put(jsonObjectEntry.getKey(), convertJsonArrayToList((JsonArray) jsonObjectEntry.getValue()));
            } else if (jsonObjectEntry.getValue() instanceof JsonObject) {
                map.put(jsonObjectEntry.getKey(), convertJsonObjectToMap((JsonObject) jsonObjectEntry.getValue()));
            } else {
                map.put(jsonObjectEntry.getKey(), jsonObjectEntry.getValue());
            }
        }

        return map;
    }

    private List<Object> convertJsonArrayToList(JsonArray jsonArray) {

        final List<Object> list = new LinkedList<>();
        for (Object jsonArrayEntry : jsonArray) {
            if(jsonArrayEntry instanceof JsonObject) {
                list.add(convertJsonObjectToMap((JsonObject) jsonArrayEntry));
            } else if(jsonArrayEntry instanceof JsonArray) {
                list.add(convertJsonArrayToList((JsonArray) jsonArrayEntry));
            } else {
                list.add(jsonArrayEntry);
            }
        }

        return list;
    }

    /**
     * Convert recursively an AggregationOption in AggregationBuilder
     * @param aggregationOption the aggregation option to parse
     * @return the created aggregation builder if everything has been parsed correctly
     */
    private AggregationBuilder parseAggregation(AggregationOption aggregationOption) {
        try {
            AggregationBuilder result = null;
            QueryParseContext context = new QueryParseContext(XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY, aggregationOption.getDefinition().encode())); 
            switch (aggregationOption.getType()) {
                case TERMS:
                    result = TermsAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case MAX:
                    result = MaxAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case MIN:
                    result = MinAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case ADJACENCY_MATRIX:
                    result = AdjacencyMatrixAggregationBuilder.getParser().parse(aggregationOption.getName(), context);
                    break;
                case SUM:
                    result = SumAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case RANGE:
                    result = RangeAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case STATS:
                    result = StatsAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case FILTER:
                    result = FilterAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case GLOBAL:
                    result = GlobalAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case NESTED:
                    result = NestedAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case AVERAGE:
                    result = AvgAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case FILTERS:
                    result = FiltersAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case MISSING:
                    result = MissingAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case SAMPLER:
                    result = SamplerAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case GEO_GRID:
                    result = GeoGridAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case IP_RANGE:
                    result = IpRangeAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case TOP_HITS:
                    result = TopHitsAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case HISTOGRAM:
                    result = HistogramAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case DATE_RANGE:
                    result = DateRangeAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case GEO_BOUNDS:
                    result = GeoBoundsAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case CARDINALITY:
                    result = CardinalityAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case PERCENTILES:
                    result = PercentilesAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case VALUE_COUNT:
                    result = ValueCountAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case GEO_CENTROID:
                    result = GeoCentroidAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case GEO_DISTANCE:
                    result = GeoDistanceAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case DATE_HISTOGRAM:
                    result = DateHistogramAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case EXTENDED_STATS:
                    result = ExtendedStatsAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case REVERSE_NESTED:
                    result = ReverseNestedAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case DIVERSIFIED_SAMPLER:
                    result = DiversifiedAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case SCRIPTED_METRIC:
                    result = ScriptedMetricAggregationBuilder.parse(aggregationOption.getName(), context);
                    break;
                case PERCENTILE_RANKS:
                    result = PercentileRanksAggregationBuilder.parse(aggregationOption.getName(), context); 
                    break;
                default:
                    throw new IllegalArgumentException("Unknown aggregation type " + aggregationOption.getType());
            }
            for (AggregationOption subAggregation : aggregationOption.getSubAggregations()) {
                result.subAggregation(parseAggregation(subAggregation));
            }

            return result;
        } catch (IOException ex) {
            System.out.println("Unable to parse the aggregations");
            throw new IllegalArgumentException("Wrong aggregation definition " + aggregationOption.getDefinition());
        }

    }

}
