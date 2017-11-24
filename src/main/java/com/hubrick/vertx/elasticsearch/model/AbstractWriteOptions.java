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

import io.vertx.core.json.JsonObject;

/**
 * Abstract options
 */
public abstract class AbstractWriteOptions<T extends AbstractWriteOptions<T>> extends AbstractOptions<T> {

    private Integer waitForActiveShard;
    private String timeout;

    public static final String FIELD_WAIT_FOR_ACTIVE_SHARD = "waitForActiveShard";
    public static final String FIELD_TIMEOUT = "timeout";

    protected AbstractWriteOptions() {
    }

    protected AbstractWriteOptions(T other) {
        super(other);
        waitForActiveShard = other.getWaitForActiveShard();
        timeout = other.getTimeout();
    }

    protected AbstractWriteOptions(JsonObject json) {
        super(json);

        timeout = json.getString(FIELD_TIMEOUT);

        String s = json.getString(FIELD_WAIT_FOR_ACTIVE_SHARD);
        if (s != null) waitForActiveShard = Integer.valueOf(s);

    }

    public Integer getWaitForActiveShard() {
        return waitForActiveShard;
    }

    public T setWaitForActiveShard(Integer waitForActiveShard) {
        this.waitForActiveShard = waitForActiveShard;
        return returnThis();
    }

    public String getTimeout() {
        return timeout;
    }

    public T setTimeout(String timeout) {
        this.timeout = timeout;
        return returnThis();
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();

        if (getWaitForActiveShard() != null) {
            json.put(FIELD_WAIT_FOR_ACTIVE_SHARD, getWaitForActiveShard().toString());
        }
        if (getTimeout() != null) json.put(FIELD_TIMEOUT, getTimeout());

        return json;
    }

}
