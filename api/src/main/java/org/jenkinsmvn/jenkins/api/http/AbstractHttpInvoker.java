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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract http invoker
 */
public abstract class AbstractHttpInvoker {

    public static final String UTF_8 = "UTF-8";

    protected HttpClient client;

    protected HttpContext context;

    protected ObjectMapper mapper;

    protected HttpHost targetHost;

    protected boolean getMethod;

    protected List<Header> headers;
    
    protected List<BasicNameValuePair> parameters;

    protected boolean forceParametersOnPath;

    public AbstractHttpInvoker(HttpClient client, HttpContext context, HttpHost targetHost, ObjectMapper mapper) {
        this(client, context, targetHost, mapper, true);
    }

    public AbstractHttpInvoker(HttpClient client, HttpContext context, HttpHost targetHost, ObjectMapper mapper, boolean getMethod) {
        this.client = client;
        this.context = context;
        this.mapper = mapper;
        this.targetHost = targetHost;
        this.getMethod = getMethod;
    }

    public void addHeader(Header header) {
        Validate.notNull(header, "header argument should not be null.");

        if(headers == null) {
            headers = new ArrayList<Header>();
        }

        headers.add(header);
    }

    public void addParameter(BasicNameValuePair nameValuePair) {
        if(parameters == null) {
            parameters = new ArrayList<BasicNameValuePair>();
        }

        parameters.add(nameValuePair);
    }

    public void forceParametersOnPath() {
        forceParametersOnPath = true;
    }

    protected HttpRequest createHttpRequest(String path) throws UnsupportedEncodingException {
        Validate.notNull(path, "path argument should not be null.");

        HttpRequest request;

        if(CollectionUtils.isNotEmpty(parameters) || (!getMethod && forceParametersOnPath)) {
            StringBuilder buf = new StringBuilder(path);

            if(buf.indexOf("?") != -1) {
                buf.append("&");
            } else {
                buf.append("?");
            }

            buf.append(URLEncodedUtils.format(parameters, UTF_8));
            path = buf.toString();
        }

        if(getMethod) {
            request = new HttpGet(path);
        } else {
            HttpPost post = new HttpPost(path);

            if(!forceParametersOnPath && CollectionUtils.isNotEmpty(parameters)) {
                post.setEntity(new UrlEncodedFormEntity(parameters, UTF_8));
            }

            request = post;
        }

        if(CollectionUtils.isNotEmpty(headers)) {
            for(Header header : headers) {
                request.addHeader(header);
            }
        }

        return request;
    }
}
