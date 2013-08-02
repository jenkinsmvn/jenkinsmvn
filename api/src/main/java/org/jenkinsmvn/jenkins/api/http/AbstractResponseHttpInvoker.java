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

import org.apache.commons.lang.Validate;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Abstract response processing
 */
public abstract class AbstractResponseHttpInvoker<T> extends AbstractHttpInvoker{

    private static final Logger LOG = Logger.getLogger(AbstractResponseHttpInvoker.class);

    protected HttpEntity responseEntity;

    public AbstractResponseHttpInvoker(HttpClient client, HttpContext context, HttpHost targetHost, ObjectMapper mapper) {
        super(client, context, targetHost, mapper, true);
    }

    public AbstractResponseHttpInvoker(HttpClient client, HttpContext context, HttpHost targetHost, ObjectMapper mapper, boolean getMethod) {
        super(client, context, targetHost, mapper, getMethod);
    }

    protected abstract T doOnResponse() throws IOException;

    public T execute(String path) throws IOException {
        Validate.notNull(path, "path argument should not be null.");

        LOG.info(String.format("Retrieving for uri '%s'.", path));

        HttpRequest request = createHttpRequest(path);

        try {
            HttpResponse response = client.execute(targetHost, request, context);
            StatusLine status = response.getStatusLine();

            if(status.getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalStateException("Error retrieving object. Status: " + status.toString());
            }

            responseEntity = response.getEntity();

            String contentType = responseEntity.getContentType().getValue().toLowerCase();

            LOG.info(String.format("Received Content-Type: %s", contentType));

            return doOnResponse();
        } finally {
            EntityUtils.consume(responseEntity);
        }
    }
}
