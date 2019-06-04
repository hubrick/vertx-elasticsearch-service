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

import com.google.common.collect.ImmutableList;
import com.hubrick.vertx.elasticsearch.ElasticSearchConfigurator;
import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.TransportClientFactory;
import com.hubrick.vertx.elasticsearch.internal.InternalElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.AbstractSearchOptions;
import com.hubrick.vertx.elasticsearch.model.AggregationOption;
import com.hubrick.vertx.elasticsearch.model.BaseSortOption;
import com.hubrick.vertx.elasticsearch.model.BaseSuggestOption;
import com.hubrick.vertx.elasticsearch.model.BulkDeleteOptions;
import com.hubrick.vertx.elasticsearch.model.BulkIndexOptions;
import com.hubrick.vertx.elasticsearch.model.BulkOptions;
import com.hubrick.vertx.elasticsearch.model.BulkUpdateOptions;
import com.hubrick.vertx.elasticsearch.model.CompletionSuggestOption;
import com.hubrick.vertx.elasticsearch.model.Conflicts;
import com.hubrick.vertx.elasticsearch.model.DeleteByQueryOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteOptions;
import com.hubrick.vertx.elasticsearch.model.FieldSortOption;
import com.hubrick.vertx.elasticsearch.model.GetOptions;
import com.hubrick.vertx.elasticsearch.model.IndexOptions;
import com.hubrick.vertx.elasticsearch.model.MultiGetOptions;
import com.hubrick.vertx.elasticsearch.model.MultiGetQueryOptions;
import com.hubrick.vertx.elasticsearch.model.MultiSearchOptions;
import com.hubrick.vertx.elasticsearch.model.MultiSearchQueryOptions;
import com.hubrick.vertx.elasticsearch.model.ScriptFieldOption;
import com.hubrick.vertx.elasticsearch.model.ScriptSortOption;
import com.hubrick.vertx.elasticsearch.model.SearchOptions;
import com.hubrick.vertx.elasticsearch.model.SearchScrollOptions;
import com.hubrick.vertx.elasticsearch.model.UpdateOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoostingQueryBuilder;
import org.elasticsearch.index.query.CommonTermsQueryBuilder;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.FieldMaskingSpanQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.GeoShapeQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.index.query.ScriptQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.index.query.SpanContainingQueryBuilder;
import org.elasticsearch.index.query.SpanFirstQueryBuilder;
import org.elasticsearch.index.query.SpanMultiTermQueryBuilder;
import org.elasticsearch.index.query.SpanNearQueryBuilder;
import org.elasticsearch.index.query.SpanNotQueryBuilder;
import org.elasticsearch.index.query.SpanOrQueryBuilder;
import org.elasticsearch.index.query.SpanTermQueryBuilder;
import org.elasticsearch.index.query.SpanWithinQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.TermsSetQueryBuilder;
import org.elasticsearch.index.query.TypeQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.ParentIdQueryBuilder;
import org.elasticsearch.percolator.PercolateQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.adjacency.AdjacencyMatrixAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.MissingAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ReverseNestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.GeoDistanceAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.IpRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
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
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.mapToBulkIndexResponse;
import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.mapToDeleteByQueryResponse;
import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.mapToDeleteResponse;
import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.mapToIndexResponse;
import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.mapToMultiGetResponse;
import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.mapToMultiSearchResponse;
import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.mapToSearchResponse;
import static com.hubrick.vertx.elasticsearch.impl.ElasticSearchServiceMapper.mapToUpdateResponse;

/**
 * Default implementation of {@link ElasticSearchService}
 */
public class DefaultElasticSearchService implements InternalElasticSearchService {

    private final Logger log = LoggerFactory.getLogger(DefaultElasticSearchService.class);
    private final TransportClientFactory clientFactory;
    private final ElasticSearchConfigurator configurator;
    protected TransportClient client;

    private static final String DEFAULT_SCRIPT_LANG = "painless";
    private static final ScriptType DEFAULT_SCRIPT_TYPE = ScriptType.INLINE;
    private static final NamedXContentRegistry DEFAULT_NAMED_X_CONTEXT_REGISTRY = new NamedXContentRegistry(
            ImmutableList.<NamedXContentRegistry.Entry>builder()
                    // Full text queries
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("match_all"), MatchAllQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("match"), MatchQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("match_phrase"), MatchPhraseQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("match_phrase_prefix"), MatchPhrasePrefixQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("multi_match"), MultiMatchQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("common"), CommonTermsQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("query_string"), QueryStringQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("simple_query_string"), SimpleQueryStringBuilder::fromXContent))

                    // Term level queries
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("term"), TermQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("terms"), TermsQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("terms_set"), TermsSetQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("range"), RangeQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("exists"), ExistsQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("prefix"), PrefixQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("wildcard"), WildcardQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("regexp"), RegexpQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("fuzzy"), FuzzyQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("type"), TypeQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("ids"), IdsQueryBuilder::fromXContent))

                    // Compound queries
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("constant_score"), ConstantScoreQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("bool"), BoolQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("dis_max"), DisMaxQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("function_score"), FunctionScoreQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("boosting"), BoostingQueryBuilder::fromXContent))

                    // Joining queries
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("nested"), NestedQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("has_child"), HasChildQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("has_parent"), HasParentQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("parent_id"), ParentIdQueryBuilder::fromXContent))

                    // Geo queries
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("geo_shape"), GeoShapeQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("geo_bounding_box"), GeoBoundingBoxQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("geo_distance"), GeoDistanceQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("geo_polygon"), GeoPolygonQueryBuilder::fromXContent))

                    // Specialized queries
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("more_like_this"), MoreLikeThisQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("script"), ScriptQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("percolate"), PercolateQueryBuilder::fromXContent))

                    // Span queries
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("span_term"), SpanTermQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("span_multi"), SpanMultiTermQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("span_first"), SpanFirstQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("span_near"), SpanNearQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("span_or"), SpanOrQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("span_not"), SpanNotQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("span_containing"), SpanContainingQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("span_within"), SpanWithinQueryBuilder::fromXContent))
                    .add(new NamedXContentRegistry.Entry(QueryBuilder.class, new ParseField("field_masking_span"), FieldMaskingSpanQueryBuilder::fromXContent))

                    .build()
    );

    @Inject
    public DefaultElasticSearchService(TransportClientFactory clientFactory, ElasticSearchConfigurator configurator) {
        this.clientFactory = clientFactory;
        this.configurator = configurator;
    }

    @Override
    public void start() {

        final Settings settings = Settings.builder()
                .put("cluster.name", configurator.getClusterName())
                .put("client.transport.sniff", true)
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

        final IndexRequestBuilder builder = client.prepareIndex(index, type).setSource(source.encode(), XContentType.JSON);
        populateIndexRequestBuilder(builder, options);

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

        final UpdateRequestBuilder builder = client.prepareUpdate(index, type, id);
        populateUpdateRequestBuilder(builder, options);

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

        final GetRequestBuilder builder = client.prepareGet(index, type, id);
        populateGetRequestBuilder(builder, options);

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

        final SearchRequestBuilder builder = client.prepareSearch(indices.toArray(new String[indices.size()]));

        if (options != null) {
            populateSearchRequestBuilder(builder, options);
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
        final SearchScrollRequestBuilder builder = client.prepareSearchScroll(scrollId);

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

        final DeleteRequestBuilder builder = client.prepareDelete(index, type, id);
        populateDeleteRequestBuilder(builder, options);

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
    public void bulk(final List<BulkIndexOptions> bulkIndexOptions,
                     final List<BulkUpdateOptions> bulkUpdateOptions,
                     final List<BulkDeleteOptions> bulkDeleteOptions,
                     final BulkOptions bulkOptions,
                     final Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.BulkResponse>> resultHandler) {
        final BulkRequestBuilder builder = client.prepareBulk();

        if (bulkOptions != null) {
            if (bulkOptions.getWaitForActiveShard() != null)
                builder.setWaitForActiveShards(bulkOptions.getWaitForActiveShard());
            if (bulkOptions.getRefreshPolicy() != null)
                builder.setRefreshPolicy(WriteRequest.RefreshPolicy.valueOf(bulkOptions.getRefreshPolicy().name()));
            if (bulkOptions.getTimeout() != null) builder.setTimeout(bulkOptions.getTimeout());
        }

        for (BulkIndexOptions bulkIndexOptionsItem : bulkIndexOptions) {
            final IndexRequestBuilder indexRequestBuilder = client.prepareIndex(bulkIndexOptionsItem.getIndex(), bulkIndexOptionsItem.getType()).setSource(convertJsonObjectToMap(bulkIndexOptionsItem.getSource()));
            populateIndexRequestBuilder(indexRequestBuilder, bulkIndexOptionsItem.getIndexOptions());
            builder.add(indexRequestBuilder);
        }

        for (BulkUpdateOptions bulkUpdateOptionsItem : bulkUpdateOptions) {
            final UpdateRequestBuilder updateBuilder = client.prepareUpdate(bulkUpdateOptionsItem.getIndex(), bulkUpdateOptionsItem.getType(), bulkUpdateOptionsItem.getId());
            populateUpdateRequestBuilder(updateBuilder, bulkUpdateOptionsItem.getUpdateOptions());
            builder.add(updateBuilder);
        }

        for (BulkDeleteOptions bulkDeleteOptionsItem : bulkDeleteOptions) {
            final DeleteRequestBuilder deleteBuilder = client.prepareDelete(bulkDeleteOptionsItem.getIndex(), bulkDeleteOptionsItem.getType(), bulkDeleteOptionsItem.getId());
            populateDeleteRequestBuilder(deleteBuilder, bulkDeleteOptionsItem.getDeleteOptions());
            builder.add(deleteBuilder);
        }

        builder.execute(new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(final BulkResponse bulkItemResponses) {
                resultHandler.handle(Future.succeededFuture(mapToBulkIndexResponse(bulkItemResponses)));
            }

            @Override
            public void onFailure(final Exception e) {
                handleFailure(resultHandler, e);
            }
        });
    }

    @Override
    public void multiSearch(final List<MultiSearchQueryOptions> multiSearchQueryOptions,
                            final MultiSearchOptions options,
                            final Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.MultiSearchResponse>> resultHandler) {

        final MultiSearchRequestBuilder builder = client.prepareMultiSearch();

        if (options != null) {
            if (options.getMaxConcurrentSearchRequests() != null) {
                builder.setMaxConcurrentSearchRequests(options.getMaxConcurrentSearchRequests());
            }

            if (options.getIndicesOptions() != null) {
                final IndicesOptions defaultIndicesOptions = IndicesOptions.strictExpandOpenAndForbidClosed();
                builder.setIndicesOptions(
                        IndicesOptions.fromOptions(
                                Optional.ofNullable(options.getIndicesOptions().getIgnoreUnavailable()).orElse(defaultIndicesOptions.ignoreUnavailable()),
                                Optional.ofNullable(options.getIndicesOptions().getAllowNoIndices()).orElse(defaultIndicesOptions.allowNoIndices()),
                                Optional.ofNullable(options.getIndicesOptions().getExpandToOpenIndices()).orElse(defaultIndicesOptions.expandWildcardsOpen()),
                                Optional.ofNullable(options.getIndicesOptions().getExpandToClosedIndices()).orElse(defaultIndicesOptions.expandWildcardsClosed()),
                                Optional.ofNullable(options.getIndicesOptions().getAllowAliasesToMultipleIndices()).orElse(defaultIndicesOptions.allowAliasesToMultipleIndices()),
                                Optional.ofNullable(options.getIndicesOptions().getForbidClosedIndices()).orElse(defaultIndicesOptions.forbidClosedIndices()),
                                Optional.ofNullable(options.getIndicesOptions().getIgnoreAliases()).orElse(defaultIndicesOptions.ignoreAliases())
                        )
                );
            }
        }

        for (MultiSearchQueryOptions multiSearchQueryOptionsItem : multiSearchQueryOptions) {
            final SearchRequestBuilder searchRequestBuilder = client.prepareSearch(multiSearchQueryOptionsItem.getIndices().toArray(new String[0]));
            populateSearchRequestBuilder(searchRequestBuilder, multiSearchQueryOptionsItem.getSearchOptions());
            builder.add(searchRequestBuilder);
        }


        builder.execute(new ActionListener<MultiSearchResponse>() {
            @Override
            public void onResponse(final MultiSearchResponse multiSearchResponse) {
                resultHandler.handle(Future.succeededFuture(mapToMultiSearchResponse(multiSearchResponse)));
            }

            @Override
            public void onFailure(final Exception e) {
                handleFailure(resultHandler, e);
            }
        });
    }

    @Override
    public void multiGet(final List<MultiGetQueryOptions> multiGetQueryOptions,
                         final MultiGetOptions options,
                         final Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.MultiGetResponse>> resultHandler) {

        final MultiGetRequestBuilder builder = client.prepareMultiGet();

        if (options != null) {
            if (options.getRefresh() != null) {
                builder.setRefresh(options.getRefresh());
            }
            if (options.getRealtime() != null) {
                builder.setRealtime(options.getRealtime());
            }
            if (options.getPreference() != null) {
                builder.setPreference(options.getPreference());
            }
        }

        for (MultiGetQueryOptions multiGetQueryOptionsItem : multiGetQueryOptions) {
            final MultiGetRequest.Item item = new MultiGetRequest.Item(multiGetQueryOptionsItem.getIndex(), multiGetQueryOptionsItem.getType(), multiGetQueryOptionsItem.getId());
            if (multiGetQueryOptionsItem.getParent() != null) item.parent(multiGetQueryOptionsItem.getParent());
            if (multiGetQueryOptionsItem.getRouting() != null) item.routing(multiGetQueryOptionsItem.getRouting());
            if (multiGetQueryOptionsItem.getStoredFields() != null)
                item.storedFields(multiGetQueryOptionsItem.getStoredFields().toArray(new String[0]));
            if (multiGetQueryOptionsItem.getFetchSource() != null) {
                item.fetchSourceContext(
                        new FetchSourceContext(
                                multiGetQueryOptionsItem.getFetchSource(),
                                multiGetQueryOptionsItem.getFetchSourceIncludes().toArray(new String[0]),
                                multiGetQueryOptionsItem.getFetchSourceExcludes().toArray(new String[0])
                        )
                );
            }

            builder.add(item);
        }

        builder.execute(new ActionListener<MultiGetResponse>() {
            @Override
            public void onResponse(final MultiGetResponse multiGetResponse) {
                resultHandler.handle(Future.succeededFuture(mapToMultiGetResponse(multiGetResponse)));
            }

            @Override
            public void onFailure(final Exception e) {
                handleFailure(resultHandler, e);
            }
        });
    }

    @Override
    public void deleteByQuery(List<String> indices, DeleteByQueryOptions options, Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse>> resultHandler) {
        final DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE);

        deleteByQueryRequestBuilder.source(indices.toArray(new String[indices.size()]));
        if (options != null) {
            populateSearchRequestBuilder(deleteByQueryRequestBuilder.source(), options);
            if (options.getMaxRetries() != null) deleteByQueryRequestBuilder.setMaxRetries(options.getMaxRetries());
            if (options.getSlices() != null) deleteByQueryRequestBuilder.setSlices(options.getSlices());
            if (options.getWaitForActiveShards() != null)
                deleteByQueryRequestBuilder.waitForActiveShards(ActiveShardCount.from(options.getWaitForActiveShards()));
            if (options.getConflicts() != null)
                deleteByQueryRequestBuilder.abortOnVersionConflict(Optional.ofNullable(options.getConflicts()).map(e -> !Conflicts.PROCEED.equals(e)).orElse(true));
            if (options.getRequestsPerSecond() != null)
                deleteByQueryRequestBuilder.setRequestsPerSecond(options.getRequestsPerSecond());
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

    private void populateIndexRequestBuilder(IndexRequestBuilder builder, IndexOptions options) {
        if (options != null) {
            if (options.getId() != null) builder.setId(options.getId());
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.getOpType() != null)
                builder.setOpType(DocWriteRequest.OpType.valueOf(options.getOpType().name()));
            if (options.getWaitForActiveShard() != null)
                builder.setWaitForActiveShards(options.getWaitForActiveShard());
            if (options.getRefreshPolicy() != null)
                builder.setRefreshPolicy(WriteRequest.RefreshPolicy.valueOf(options.getRefreshPolicy().name()));
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());
        }
    }

    private void populateGetRequestBuilder(GetRequestBuilder builder, GetOptions options) {
        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.getRefresh() != null) builder.setRefresh(options.getRefresh());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());

            if (options.getPreference() != null) builder.setPreference(options.getPreference());
            if (!options.getFields().isEmpty()) {
                builder.setStoredFields(options.getFields().toArray(new String[options.getFields().size()]));
            }
            if (options.getFetchSource() != null) builder.setFetchSource(options.getFetchSource());
            if (!options.getFetchSourceIncludes().isEmpty() || !options.getFetchSourceExcludes().isEmpty()) {
                String[] includes = options.getFetchSourceIncludes().toArray(new String[options.getFetchSourceIncludes().size()]);
                String[] excludes = options.getFetchSourceExcludes().toArray(new String[options.getFetchSourceExcludes().size()]);
                builder.setFetchSource(includes, excludes);
            }
            if (options.getRealtime() != null) builder.setRealtime(options.getRealtime());
        }
    }

    private void populateDeleteRequestBuilder(DeleteRequestBuilder builder, DeleteOptions options) {
        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.getRefreshPolicy() != null)
                builder.setRefreshPolicy(WriteRequest.RefreshPolicy.valueOf(options.getRefreshPolicy().name()));
            if (options.getWaitForActiveShard() != null)
                builder.setWaitForActiveShards(options.getWaitForActiveShard());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());
        }
    }


    private void populateUpdateRequestBuilder(UpdateRequestBuilder builder, UpdateOptions options) {
        if (options != null) {
            if (options.getRouting() != null) builder.setRouting(options.getRouting());
            if (options.getParent() != null) builder.setParent(options.getParent());
            if (options.getRefreshPolicy() != null)
                builder.setRefreshPolicy(WriteRequest.RefreshPolicy.valueOf(options.getRefreshPolicy().name()));
            if (options.getWaitForActiveShard() != null)
                builder.setWaitForActiveShards(options.getWaitForActiveShard());
            if (options.getVersion() != null) builder.setVersion(options.getVersion());
            if (options.getVersionType() != null) builder.setVersionType(options.getVersionType());
            if (options.getTimeout() != null) builder.setTimeout(options.getTimeout());

            if (options.getRetryOnConflict() != null) builder.setRetryOnConflict(options.getRetryOnConflict());
            if (options.getDoc() != null) builder.setDoc(options.getDoc().encode(), XContentType.JSON);
            if (options.getUpsert() != null) builder.setUpsert(options.getUpsert().encode(), XContentType.JSON);
            if (options.getDocAsUpsert() != null) builder.setDocAsUpsert(options.getDocAsUpsert());
            if (options.getDetectNoop() != null) builder.setDetectNoop(options.getDetectNoop());
            if (options.getScriptedUpsert() != null) builder.setScriptedUpsert(options.getScriptedUpsert());

            if (options.getScript() != null) {
                builder.setScript(createScript(Optional.ofNullable(options.getScriptType()), Optional.ofNullable(options.getScriptLang()), Optional.ofNullable(options.getScriptParams()), options.getScript()));

            }
            if (!options.getFields().isEmpty()) {
                builder.setFields(options.getFields().toArray(new String[options.getFields().size()]));
            }
        }
    }

    private void populateSearchRequestBuilder(SearchRequestBuilder builder, AbstractSearchOptions options) {
        if (!options.getTypes().isEmpty()) {
            builder.setTypes((String[]) options.getTypes().toArray(new String[options.getTypes().size()]));
        }
        if (options.getSearchType() != null) builder.setSearchType(SearchType.valueOf(options.getSearchType().name()));
        if (options.getScroll() != null) builder.setScroll(options.getScroll());
        if (options.getTimeoutInMillis() != null)
            builder.setTimeout(TimeValue.timeValueMillis(options.getTimeoutInMillis()));
        if (options.getTerminateAfter() != null) builder.setTerminateAfter(options.getTerminateAfter());
        if (options.getRouting() != null) builder.setRouting(options.getRouting());
        if (options.getPreference() != null) builder.setPreference(options.getPreference());
        if (options.getQuery() != null) builder.setQuery(QueryBuilders.wrapperQuery(options.getQuery().encode()));
        if (options.getPostFilter() != null)
            builder.setPostFilter(QueryBuilders.wrapperQuery(options.getPostFilter().encode()));
        if (options.getMinScore() != null) builder.setMinScore(options.getMinScore());
        if (options.getSize() != null) builder.setSize(options.getSize());
        if (options.getFrom() != null) builder.setFrom(options.getFrom());
        if (options.isExplain() != null) builder.setExplain(options.isExplain());
        if (options.isVersion() != null) builder.setVersion(options.isVersion());
        if (options.isFetchSource() != null) builder.setFetchSource(options.isFetchSource());
        if (options.isTrackScores() != null) builder.setTrackScores(options.isTrackScores());
        if (!options.getStoredFields().isEmpty()) {
            builder.storedFields((String[]) options.getStoredFields().toArray(new String[options.getTypes().size()]));
        }

        if (options.getIndicesOptions() != null) {
            final IndicesOptions defaultIndicesOptions = IndicesOptions.strictExpandOpenAndForbidClosed();
            builder.setIndicesOptions(
                    IndicesOptions.fromOptions(
                            Optional.ofNullable(options.getIndicesOptions().getIgnoreUnavailable()).orElse(defaultIndicesOptions.ignoreUnavailable()),
                            Optional.ofNullable(options.getIndicesOptions().getAllowNoIndices()).orElse(defaultIndicesOptions.allowNoIndices()),
                            Optional.ofNullable(options.getIndicesOptions().getExpandToOpenIndices()).orElse(defaultIndicesOptions.expandWildcardsOpen()),
                            Optional.ofNullable(options.getIndicesOptions().getExpandToClosedIndices()).orElse(defaultIndicesOptions.expandWildcardsClosed()),
                            Optional.ofNullable(options.getIndicesOptions().getAllowAliasesToMultipleIndices()).orElse(defaultIndicesOptions.allowAliasesToMultipleIndices()),
                            Optional.ofNullable(options.getIndicesOptions().getForbidClosedIndices()).orElse(defaultIndicesOptions.forbidClosedIndices()),
                            Optional.ofNullable(options.getIndicesOptions().getIgnoreAliases()).orElse(defaultIndicesOptions.ignoreAliases())
                    )
            );
        }

        if (!options.getSourceIncludes().isEmpty() || !options.getSourceExcludes().isEmpty()) {
            builder.setFetchSource(
                    (String[]) options.getSourceIncludes().toArray(new String[options.getSourceIncludes().size()]),
                    (String[]) options.getSourceExcludes().toArray(new String[options.getSourceExcludes().size()])
            );
        }

        if (options.getAggregations() != null) {
            options.getAggregations().forEach(aggregationOption -> {
                builder.addAggregation(parseAggregation((AggregationOption) aggregationOption));
            });
        }
        if (!options.getSorts().isEmpty()) {
            for (BaseSortOption baseSortOption : (List<BaseSortOption>) options.getSorts()) {
                switch (baseSortOption.getSortType()) {
                    case FIELD:
                        final FieldSortOption fieldSortOption = (FieldSortOption) baseSortOption;
                        builder.addSort(fieldSortOption.getField(), SortOrder.valueOf(fieldSortOption.getOrder().name()));
                        break;
                    case SCRIPT:
                        final ScriptSortOption scriptSortOption = (ScriptSortOption) baseSortOption;
                        final Script script = createScript(Optional.ofNullable(scriptSortOption.getScriptType()), Optional.ofNullable(scriptSortOption.getLang()), Optional.ofNullable(scriptSortOption.getParams()), scriptSortOption.getScript());
                        final ScriptSortBuilder scriptSortBuilder = new ScriptSortBuilder(script, ScriptSortBuilder.ScriptSortType.fromString(scriptSortOption.getType().getValue())).order(SortOrder.valueOf(scriptSortOption.getOrder().name()));
                        builder.addSort(scriptSortBuilder);
                        break;
                }
            }
        }

        if (!options.getScriptFields().isEmpty()) {
            options.getScriptFields().forEach((scriptNameObject, scriptValueObject) -> {
                final String scriptName = (String) scriptNameObject;
                final ScriptFieldOption scriptValue = (ScriptFieldOption) scriptValueObject;
                final Script script = createScript(Optional.ofNullable(scriptValue.getScriptType()), Optional.ofNullable(scriptValue.getLang()), Optional.ofNullable(scriptValue.getParams()), scriptValue.getScript());
                builder.addScriptField(scriptName, script);
            });
        }

        for (Map.Entry<String, BaseSuggestOption> suggestOptionEntry : ((Map<String, BaseSuggestOption>) options.getSuggestions()).entrySet()) {
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

                    builder.suggest(new SuggestBuilder().addSuggestion(suggestOptionEntry.getKey(), completionBuilder));
                    break;
            }
        }

        if (options.hasSearchAfter()) {
            builder.searchAfter(options.getSearchAfter().stream().toArray());
        }
    }

    private Script createScript(Optional<com.hubrick.vertx.elasticsearch.model.ScriptType> type, Optional<String> lang, Optional<JsonObject> params, String script) {
        final Map<String, Object> paramsMap = params.map(this::convertJsonObjectToMap).orElse(Collections.emptyMap());
        final ScriptType scriptType = type.map(e -> ScriptType.valueOf(e.name())).orElse(DEFAULT_SCRIPT_TYPE);
        final String scriptLang = lang.orElse(DEFAULT_SCRIPT_LANG);
        return new Script(scriptType, scriptLang, script, paramsMap);
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
            if (jsonArrayEntry instanceof JsonObject) {
                list.add(convertJsonObjectToMap((JsonObject) jsonArrayEntry));
            } else if (jsonArrayEntry instanceof JsonArray) {
                list.add(convertJsonArrayToList((JsonArray) jsonArrayEntry));
            } else {
                list.add(jsonArrayEntry);
            }
        }

        return list;
    }

    /**
     * Convert recursively an AggregationOption in AggregationBuilder
     *
     * @param aggregationOption the aggregation option to parse
     * @return the created aggregation builder if everything has been parsed correctly
     */
    private AggregationBuilder parseAggregation(AggregationOption aggregationOption) {
        try {
            final XContentParser context = XContentType.JSON.xContent().createParser(DEFAULT_NAMED_X_CONTEXT_REGISTRY, aggregationOption.getDefinition().encode());

            // Skip START_OBJECT. The aggregation builders don't like it
            context.nextToken();

            final AggregationBuilder result;
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
                    result = AdjacencyMatrixAggregationBuilder.parse(aggregationOption.getName(), context);
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
