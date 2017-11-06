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

import com.hubrick.vertx.elasticsearch.model.*;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.term.TermSuggestion;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
public class ElasticSearchServiceMapper {

    public static com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse mapToDeleteByQueryResponse(BulkByScrollResponse esDeleteByQueryResponse) {
        final com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse deleteByQueryResponse = new com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse();
        deleteByQueryResponse.setRawResponse(readResponse(esDeleteByQueryResponse));
        deleteByQueryResponse.setTook(esDeleteByQueryResponse.getTook().getMillis());
        deleteByQueryResponse.setTimedOut(esDeleteByQueryResponse.isTimedOut());
        deleteByQueryResponse.setDeleted(esDeleteByQueryResponse.getDeleted());
        deleteByQueryResponse.setBatches(esDeleteByQueryResponse.getBatches());
        deleteByQueryResponse.setRetries(esDeleteByQueryResponse.getBulkRetries());
        deleteByQueryResponse.setThrottled(esDeleteByQueryResponse.getStatus().getThrottled().getMillis());
        deleteByQueryResponse.setVersionConflicts(esDeleteByQueryResponse.getVersionConflicts());
        if (esDeleteByQueryResponse.getBulkFailures() != null) {
            esDeleteByQueryResponse.getBulkFailures().forEach(failure -> {
                deleteByQueryResponse.addFailure(new JsonObject(failure.toString()));
            });
        }

        return deleteByQueryResponse;
    }

    public static com.hubrick.vertx.elasticsearch.model.DeleteResponse mapToDeleteResponse(DeleteResponse esDeleteResponse) {
        final com.hubrick.vertx.elasticsearch.model.DeleteResponse deleteResponse = new com.hubrick.vertx.elasticsearch.model.DeleteResponse();

        deleteResponse.setShards(mapToShards(esDeleteResponse.getShardInfo()));
        deleteResponse.setIndex(esDeleteResponse.getIndex());
        deleteResponse.setType(esDeleteResponse.getType());
        deleteResponse.setId(esDeleteResponse.getId());
        deleteResponse.setVersion(esDeleteResponse.getVersion());
        deleteResponse.setFound(esDeleteResponse.status().equals(RestStatus.FOUND));

        return deleteResponse;
    }

    public static com.hubrick.vertx.elasticsearch.model.GetResponse mapToUpdateResponse(GetResponse esGetResponse) {
        final com.hubrick.vertx.elasticsearch.model.GetResponse getResponse = new com.hubrick.vertx.elasticsearch.model.GetResponse();

        //getResponse.setRawResponse(readResponse(esGetResponse)); // crashes...
        getResponse.setRawResponse(new JsonObject(esGetResponse.toString()));
        getResponse.setResult(mapToGetResult(esGetResponse));

        return getResponse;
    }

    public static com.hubrick.vertx.elasticsearch.model.UpdateResponse mapToUpdateResponse(UpdateResponse esUpdateResponse) {
        final com.hubrick.vertx.elasticsearch.model.UpdateResponse updateResponse = new com.hubrick.vertx.elasticsearch.model.UpdateResponse();

        updateResponse.setShards(mapToShards(esUpdateResponse.getShardInfo()));
        updateResponse.setIndex(esUpdateResponse.getIndex());
        updateResponse.setType(esUpdateResponse.getType());
        updateResponse.setId(esUpdateResponse.getId());
        updateResponse.setVersion(esUpdateResponse.getVersion());
        updateResponse.setCreated(esUpdateResponse.status().equals(RestStatus.CREATED));

        if (esUpdateResponse.getGetResult() != null) {
            updateResponse.setResult(mapToGetResult(esUpdateResponse.getGetResult()));
        }

        return updateResponse;
    }

    public static com.hubrick.vertx.elasticsearch.model.IndexResponse mapToIndexResponse(IndexResponse esIndexResponse) {
        final com.hubrick.vertx.elasticsearch.model.IndexResponse indexResponse = new com.hubrick.vertx.elasticsearch.model.IndexResponse();

        indexResponse.setShards(mapToShards(esIndexResponse.getShardInfo()));
        indexResponse.setIndex(esIndexResponse.getIndex());
        indexResponse.setType(esIndexResponse.getType());
        indexResponse.setId(esIndexResponse.getId());
        indexResponse.setVersion(esIndexResponse.getVersion());
        indexResponse.setCreated(esIndexResponse.status().equals(RestStatus.CREATED));

        return indexResponse;
    }

    public static com.hubrick.vertx.elasticsearch.model.SuggestResponse mapToSuggestResponse(SearchResponse esSuggestResponse) {
        final com.hubrick.vertx.elasticsearch.model.SuggestResponse searchResponse = new com.hubrick.vertx.elasticsearch.model.SuggestResponse();

        searchResponse.setRawResponse(new JsonObject(esSuggestResponse.toString()));
        searchResponse.setShards(mapToShards(esSuggestResponse));

        if (esSuggestResponse.getSuggest() != null) {
            for (Suggest.Suggestion<?> entries : esSuggestResponse.getSuggest()) {
                searchResponse.addSuggestion(entries.getName(), mapToSuggestion(entries));
            }
        }

        return searchResponse;
    }

    public static com.hubrick.vertx.elasticsearch.model.SearchResponse mapToSearchResponse(SearchResponse esSearchResponse) {
        final com.hubrick.vertx.elasticsearch.model.SearchResponse searchResponse = new com.hubrick.vertx.elasticsearch.model.SearchResponse();

        //searchResponse.setRawResponse(readResponse(esSearchResponse)); // crashes...
        searchResponse.setRawResponse(new JsonObject(esSearchResponse.toString()));
        searchResponse.setTook(esSearchResponse.getTookInMillis());
        searchResponse.setTimedOut(esSearchResponse.isTimedOut());
        searchResponse.setShards(mapToShards(esSearchResponse));
        searchResponse.setHits(mapToHits(esSearchResponse.getHits()));
        searchResponse.setScrollId(esSearchResponse.getScrollId());

        if (esSearchResponse.getSuggest() != null) {
            for (Suggest.Suggestion<?> entries : esSearchResponse.getSuggest()) {
                searchResponse.addSuggestion(entries.getName(), mapToSuggestion(entries));
            }
        }

        if (esSearchResponse.getAggregations() != null) {
            searchResponse.setAggregations(
                    esSearchResponse.getAggregations().asMap()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(e -> e.getKey(), e -> readResponse((InternalAggregation) e.getValue())))
            );
        }

        return searchResponse;
    }

    private static Shards mapToShards(ReplicationResponse.ShardInfo shardInfo) {
        return new Shards()
                .setFailed(shardInfo.getFailed())
                .setSuccessful(shardInfo.getSuccessful())
                .setTotal(shardInfo.getTotal());
    }

    private static Shards mapToShards(SearchResponse esSearchResponse) {
        return new Shards()
                .setFailed(esSearchResponse.getFailedShards())
                .setSuccessful(esSearchResponse.getSuccessfulShards())
                .setTotal(esSearchResponse.getTotalShards());
    }

    private static Shards mapToShards(BroadcastResponse esSearchResponse) {
        return new Shards()
                .setFailed(esSearchResponse.getFailedShards())
                .setSuccessful(esSearchResponse.getSuccessfulShards())
                .setTotal(esSearchResponse.getTotalShards());
    }

    private static Hits mapToHits(SearchHits searchHits) {
        return new Hits()
                .setMaxScore(searchHits.getMaxScore())
                .setTotal(searchHits.getTotalHits())
                .setHits(
                        Arrays.asList(searchHits.getHits())
                                .stream()
                                .map(ElasticSearchServiceMapper::mapToHit)
                                .collect(Collectors.toList())
                );
    }

    private static Hit mapToHit(SearchHit searchHit) {
        final Hit hit = new Hit()
                .setId(searchHit.getId())
                .setIndex(searchHit.getIndex())
                .setType(searchHit.getType())
                .setScore(searchHit.getScore())
                .setVersion(searchHit.getVersion())
                .setFields(
                        searchHit.getFields()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getValues()))
                );

        if (searchHit.getSource() != null) {
            hit.setSource(new JsonObject(searchHit.getSource()));
        }

        return hit;
    }

    private static com.hubrick.vertx.elasticsearch.model.GetResult mapToGetResult(GetResponse getResponse) {
        final com.hubrick.vertx.elasticsearch.model.GetResult getResult = new com.hubrick.vertx.elasticsearch.model.GetResult()
                .setId(getResponse.getId())
                .setIndex(getResponse.getIndex())
                .setType(getResponse.getType())
                .setVersion(getResponse.getVersion())
                .setExists(getResponse.isExists());

        if (getResponse.getFields() != null) {
            getResult.setFields(
                    getResponse.getFields()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getValues()))
            );
        }

        if (getResponse.getSource() != null) {
            getResult.setSource(new JsonObject(getResponse.getSource()));
        }

        return getResult;
    }

    private static com.hubrick.vertx.elasticsearch.model.GetResult mapToGetResult(GetResult esGetResult) {
        final com.hubrick.vertx.elasticsearch.model.GetResult getResult = new com.hubrick.vertx.elasticsearch.model.GetResult()
                .setId(esGetResult.getId())
                .setIndex(esGetResult.getIndex())
                .setType(esGetResult.getType())
                .setVersion(esGetResult.getVersion())
                .setExists(esGetResult.isExists());

        if (esGetResult.getFields() != null) {
            getResult.setFields(
                    esGetResult.getFields()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getValues()))
            );
        }

        if (esGetResult.getSource() != null) {
            getResult.setSource(new JsonObject(esGetResult.getSource()));
        }

        return getResult;
    }

    private static Suggestion mapToSuggestion(Suggest.Suggestion<?> esSuggestion) {
        final Suggestion suggestion = new Suggestion()
                .setName(esSuggestion.getName())
                .setSize(esSuggestion.getEntries().size())
                .setEntries(
                        esSuggestion.getEntries().stream()
                                .map(e -> mapToSuggestionEntry(e))
                                .collect(Collectors.toList())
                );

        if (esSuggestion instanceof CompletionSuggestion) {
            suggestion.setSuggestionType(SuggestionType.COMPLETION);
        } else if (esSuggestion instanceof PhraseSuggestion) {
            suggestion.setSuggestionType(SuggestionType.PHRASE);
        } else if (esSuggestion instanceof TermSuggestion) {
            suggestion.setSuggestionType(SuggestionType.TERM);
        } else {
            throw new RuntimeException("SuggestionType " + esSuggestion.getClass().getCanonicalName() + " unknown");
        }

        return suggestion;
    }

    private static SuggestionEntry mapToSuggestionEntry(Suggest.Suggestion.Entry<?> esSuggestionEntry) {
        final SuggestionEntry suggestionEntry = new SuggestionEntry()
                .setLength(esSuggestionEntry.getLength())
                .setOffset(esSuggestionEntry.getOffset())
                .setOptions(
                        esSuggestionEntry.getOptions()
                                .stream()
                                .map(e -> mapToSuggestionEntryOption(e))
                                .collect(Collectors.toList())
                );

        if (esSuggestionEntry.getText() != null) {
            suggestionEntry.setText(esSuggestionEntry.getText().string());
        }


        return suggestionEntry;
    }

    private static SuggestionEntryOption mapToSuggestionEntryOption(Suggest.Suggestion.Entry.Option esSuggestionEntryOption) {
        final SuggestionEntryOption suggestionEntryOption = new SuggestionEntryOption()
                .setScore(esSuggestionEntryOption.getScore());

        if (esSuggestionEntryOption.getText() != null) {
            suggestionEntryOption.setText(esSuggestionEntryOption.getText().string());
        }

        if (esSuggestionEntryOption.getHighlighted() != null) {
            suggestionEntryOption.setHighlight(esSuggestionEntryOption.getHighlighted().string());
        }

        return suggestionEntryOption;
    }

    protected static JsonObject readResponse(ToXContent toXContent) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            toXContent.toXContent(builder, SearchResponse.EMPTY_PARAMS);
            builder.endObject();

            return new JsonObject(builder.string());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
