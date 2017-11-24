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

import com.hubrick.vertx.elasticsearch.model.UpdateOptions;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.script.ScriptType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link UpdateOptions}
 */
public class UpdateOptionsTest {

    @Test
    public void testUpdateOptions() throws Exception {

        UpdateOptions options1 = new UpdateOptions()
                .setScript("script", ScriptType.INLINE);

        JsonObject json1 = options1.toJson();
        assertEquals(2, json1.fieldNames().size());

        UpdateOptions options2 = new UpdateOptions(json1);
        JsonObject json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options1 = new UpdateOptions()
                .setScript("script", ScriptType.INLINE)
                .setScriptLang("js")
                .setScriptParams(new JsonObject().put("p1", "1").put("p2", 2))
                .addField("field1")
                .addField("field2")
                .setRetryOnConflict(2)
                .setDoc(new JsonObject().put("field1", "1"))
                .setUpsert(new JsonObject().put("field2", "2"))
                .setDocAsUpsert(true)
                .setDetectNoop(false)
                .setScriptedUpsert(true);

        json1 = options1.toJson();
        assertEquals(11, json1.fieldNames().size());

        options2 = new UpdateOptions(json1);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options2 = new UpdateOptions(options1);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

    }
}
