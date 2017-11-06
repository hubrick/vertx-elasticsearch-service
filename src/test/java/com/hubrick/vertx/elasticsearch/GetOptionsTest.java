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

import com.hubrick.vertx.elasticsearch.model.GetOptions;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link GetOptions}
 */
public class GetOptionsTest {

    @Test
    public void testToJson() throws Exception {

        GetOptions options1 = new GetOptions()
                .setPreference("preference");
        JsonObject json1 = options1.toJson();

        assertEquals(1, json1.fieldNames().size());
        assertEquals("{\"preference\":\"preference\"}", json1.encode());

        options1 = new GetOptions()
                .setPreference("preference")
                .addField("field1")
                .addField("field2")
                .setFetchSource(true)
                .setFetchSource(Arrays.asList("incl1", "incl2"), Arrays.asList("excl1", "excl2"))
                .setRealtime(true);

        json1= options1.toJson();

        assertEquals(6, json1.fieldNames().size());

        GetOptions options2 = new GetOptions(json1);
        JsonObject json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options2 = new GetOptions(options1);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

    }
}
