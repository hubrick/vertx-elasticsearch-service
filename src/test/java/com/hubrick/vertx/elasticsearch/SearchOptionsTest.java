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

import com.google.common.collect.ImmutableList;
import com.hubrick.vertx.elasticsearch.model.ScriptSortOption;
import com.hubrick.vertx.elasticsearch.model.SearchOptions;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link SearchOptions}
 */
public class SearchOptionsTest {

    @Test
    public void testSearchOptions() throws Exception {

        SearchOptions options1 = new SearchOptions().addType("type1");
        JsonObject json1 = options1.toJson();

        assertEquals("{\"types\":[\"type1\"]}", json1.encode());

        options1 = new SearchOptions()
                .addType("type1")
                .addType("type2")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setScroll("scroll")
                .setTimeout("timeout")
                .setTerminateAfter(10)
                .setRouting("routing")
                .setPreference("preference")
                .setQuery(new JsonObject().put("status", "ok"))
                .setPostFilter(new JsonObject().put("status2", "ok"))
                .setMinScore(10F)
                .setSize(50)
                .setFrom(1)
                .setExplain(true)
                .setVersion(true)
                .setFetchSource(true)
                .addField("field1")
                .addField("field2")
                .setTrackScores(true)
                //.addAggregation(AggregationBuilders.terms("name"))
                .addFieldSort("status", SortOrder.ASC)
                .addFieldSort("insert_date", SortOrder.ASC)
                .addScripSort("doc['score']", ScriptSortOption.Type.NUMBER, new JsonObject(), SortOrder.ASC)
                .addScriptField("script_field", "doc['score']", new JsonObject().put("param1", ImmutableList.of("1", "2", "3")));

        json1 = options1.toJson();

        SearchOptions options2 = new SearchOptions(options1);
        JsonObject json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options2 = new SearchOptions(json2);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());
    }

}
