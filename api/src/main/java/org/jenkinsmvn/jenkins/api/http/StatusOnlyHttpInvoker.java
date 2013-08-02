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

import java.io.IOException;

/**
 * Http invoker that validates the received http status
 */
public class StatusOnlyHttpInvoker extends AbstractHttpInvoker {
    private static final Logger LOG = Logger.getLogger(GetJsonObject.class);

    protected HttpEntity responseEntity;

    protected int httpStatus = HttpStatus.SC_OK;
    
    protected int receivedStatus;

    public StatusOnlyHttpInvoker(HttpClient client, HttpContext context, HttpHost targetHost) {
        this(client, context, targetHost, true);
    }

    public StatusOnlyHttpInvoker(HttpClient client, HttpContext context, HttpHost targetHost, boolean getMethod) {
        super(client, context, targetHost, null, getMethod);
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getReceivedStatus() {
        return receivedStatus;
    }

    public boolean execute(String path) throws IOException {
        Validate.notNull(path, "path should not be null.");

        LOG.info(String.format("Retrieving for uri '%s'.", path));

        HttpRequest request = createHttpRequest(path);

        try {
            HttpResponse response = client.execute(targetHost, request, context);
            StatusLine status = response.getStatusLine();

            responseEntity = response.getEntity();
            receivedStatus = status.getStatusCode();

            return receivedStatus == httpStatus;
        } finally {
            EntityUtils.consume(responseEntity);
        }
    }
}
