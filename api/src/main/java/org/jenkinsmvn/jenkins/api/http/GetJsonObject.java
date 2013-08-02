/*
 * Copyright (c) 2013. Jenkinsmvn. All Rights Reserved.
 *
 * See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Jenkinsmvn licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsmvn.jenkins.api.http;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Retrieves a JSON object using http client.
 */
public class GetJsonObject<T> extends AbstractResponseHttpInvoker<T> {

    private Class<T> clazz;


    public GetJsonObject(HttpClient client, HttpContext context, HttpHost targetHost, ObjectMapper mapper, Class<T> clazz) {
        super(client, context, targetHost, mapper);
        this.clazz = clazz;
    }

    public T getObject(String path) throws IOException {
        return execute(path);
    }

    @Override
    protected T doOnResponse() throws IOException {
        return mapper.readValue(responseEntity.getContent(), clazz);
    }
}
