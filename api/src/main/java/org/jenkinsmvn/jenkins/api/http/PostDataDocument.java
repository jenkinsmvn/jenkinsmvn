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
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.UnsupportedEncodingException;

/**
 * Post the given data
 */
public class PostDataDocument extends StatusOnlyHttpInvoker {
    
    private String data;

    public PostDataDocument(HttpClient client, HttpContext context, HttpHost targetHost) {
        super(client, context, targetHost, false);
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    protected HttpRequest createHttpRequest(String path) throws UnsupportedEncodingException {
        Validate.notNull(path, "path should not be null.");
        Validate.notNull(data, "data should not be null.");

        HttpPost post = new HttpPost(path);

        StringEntity entity = new StringEntity(data, "utf-8");
        post.setEntity(entity);

        return post;
    }
}
