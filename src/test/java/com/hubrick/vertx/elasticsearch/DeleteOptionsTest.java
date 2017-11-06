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

import com.hubrick.vertx.elasticsearch.model.DeleteOptions;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.index.VersionType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DeleteOptions}
 */
public class DeleteOptionsTest {

    @Test
    public void testToJson() throws Exception {

        DeleteOptions options1 = new DeleteOptions()
                .setParent("parent");
        JsonObject json1 = options1.toJson();

        assertEquals("{\"parent\":\"parent\"}", json1.encode());

        options1 = new DeleteOptions()
                .setParent("parent")
                .setRefresh(true)
                .setRouting("routing")
                .setTimeout("timeout")
                .setVersion(10000L)
                .setVersionType(VersionType.EXTERNAL);

        json1 = options1.toJson();

        assertEquals(6, json1.fieldNames().size());

        DeleteOptions options2 = new DeleteOptions(json1);
        JsonObject json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options2 = new DeleteOptions(options1);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

    }

}
