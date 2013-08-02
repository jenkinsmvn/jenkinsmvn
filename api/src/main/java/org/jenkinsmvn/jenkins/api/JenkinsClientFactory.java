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

package org.jenkinsmvn.jenkins.api;

import org.apache.commons.lang.Validate;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.net.URI;

/**
 * Jenkins client factory
 */
public final class JenkinsClientFactory {

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.getSerializationConfig().withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

        return mapper;
    }

    public static AbstractHttpClient createHttpClient(int defaultMaxPerRoute, int maxTotal) {
        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager();
        manager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        manager.setMaxTotal(maxTotal);

        return new DefaultHttpClient(manager);
    }

    public static JenkinsClient create(String path, int defaultMaxPerRoute, int maxTotal) throws IOException {
        return create(path, null, defaultMaxPerRoute, maxTotal);
    }

    public static JenkinsClient create(String path, String contextPath, int defaultMaxPerRoute, int maxTotal) throws IOException {
        Validate.notNull(path, "path should not be null.");

        HttpContext context = new BasicHttpContext();

        return new JenkinsClient(createHttpClient(defaultMaxPerRoute, maxTotal), context, createObjectMapper(), URI.create(path), contextPath);
    }


    public static JenkinsClient create(String path) throws IOException {
        return create(path, null, 2, 2);
    }

    public static JenkinsClient create(String path, String contextPath) throws IOException {
        return create(path, contextPath, 2, 2);
    }
}
