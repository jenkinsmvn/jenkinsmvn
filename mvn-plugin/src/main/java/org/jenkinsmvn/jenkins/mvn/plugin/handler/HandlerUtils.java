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

package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.JenkinsClient;
import org.jenkinsmvn.jenkins.api.JenkinsClientFactory;
import org.jenkinsmvn.jenkins.api.http.StatusOnlyHttpInvoker;
import org.jenkinsmvn.jenkins.api.model.Job;
import org.jenkinsmvn.jenkins.mvn.plugin.Action;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.*;

/**
 * Handler utils
 */
public enum HandlerUtils {
    INSTANCE;

    public StatusOnlyHttpInvoker createVerifyStatusInvoker(Action action, URL url, int status) throws URISyntaxException {
        URI uri = url.toURI();
        HttpClient client = JenkinsClientFactory.createHttpClient(1, 1);
        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

        StatusOnlyHttpInvoker invoker = new StatusOnlyHttpInvoker(client, new BasicHttpContext(), targetHost);
        invoker.setHttpStatus(status);

        // for authorization
        if(isPropertyExist(action, HTTP_AUTHORIZATION)) {
            invoker.addHeader(new BasicHeader("Authorization", getRequiredProperty(action, HTTP_AUTHORIZATION)));
        }

        return invoker;
    }

    public String prettyPrintElapse(long elapse) {
        long minutes = 0;
        long seconds = 0;

        if(elapse > 0) {
            // determine the minutes value
            // minute is equal to 1000 * 60 millis
            minutes = elapse / (1000 * 60);
            elapse -= minutes * (1000 * 60);
        }
        if(elapse > 0) {
            // determine the seconds value
            // seconds is equal to 1000 millis
            seconds = elapse / 1000;
            elapse -= seconds * 1000;
        }

        StringBuilder buf = new StringBuilder();

        if(minutes > 0) buf.append(buf.length() > 0 ? " " : "").append(minutes).append("m");
        if(seconds > 0) buf.append(buf.length() > 0 ? " " : "").append(seconds).append("s");
        if(elapse > 0)    buf.append(buf.length() > 0 ? " " : "").append(elapse).append("ms");

        if(buf.length() <= 0) {
            buf.append("0ms");
        }

        return buf.toString();
    }
    
    public boolean isPropertyExist(Action action, String name) {
        if(MapUtils.isEmpty(action.getProperties())) {
            return false;
        }

        String value = StringUtils.trim(action.getProperties().getProperty(name));
        return StringUtils.isNotBlank(value);
    }
    
    public Map<String, String> createParameters(Action action, String prefix) {
        Map<String, String> parameters = new HashMap<String, String>();

        if(MapUtils.isEmpty(action.getProperties())) {
            return null;
        }

        for(Map.Entry<Object, Object> entry : action.getProperties().entrySet()) {
            String name = StringUtils.trim(String.valueOf(entry.getKey()));
            String value = StringUtils.trim(String.valueOf(entry.getValue()));

            if(StringUtils.startsWith(name, prefix)) {
                String paramName = name.substring(prefix.length());
                parameters.put(paramName, value);
            }
        }

        if(MapUtils.isEmpty(parameters)) {
            return null;
        }

        return parameters;
    }

    public Boolean getRequiredBoolean(Action action, String name) {
        return Boolean.parseBoolean(HandlerUtils.INSTANCE.getRequiredProperty(action, name));
    }
    
    public Integer getRequiredInteger(Action action, String name) {
        try {
            return Integer.parseInt(HandlerUtils.INSTANCE.getRequiredProperty(action, name));
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Property '%s' for action '%s' is not a valid number.", name, action.getName()));
        }
    }
    
    public String getRequiredProperty(Action action, String name) {
        if(MapUtils.isEmpty(action.getProperties())) {
            throw new IllegalArgumentException(String.format("Required properties for action '%s'.", action.getName()));
        }

        String value = StringUtils.trim(action.getProperties().getProperty(name));

        if(StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(String.format("Required property '%s' for action '%s'.", name, action.getName()));
        }

        return value;
    }
    
    public String getReplacement(String orig, Action action) {
        String replacement = getRequiredProperty(action, REPLACEMENT);

        // direct replacement
        if(!isPropertyExist(action, REGEX_REPLACE_PATTERN)) {
            return replacement;
        }

        String regexReplacePattern = getRequiredProperty(action, REGEX_REPLACE_PATTERN);
        int group = -1;
        
        if(isPropertyExist(action, REGEX_REPLACE_PATTERN_GROUP)) {
            try {
                group = Integer.parseInt(getRequiredProperty(action, REGEX_REPLACE_PATTERN_GROUP));
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Property '%s' for action '%s' is not a integer.", REGEX_REPLACE_PATTERN_GROUP, action.getName()));
            }
        }

        Pattern pattern = Pattern.compile(regexReplacePattern);
        Matcher matcher = pattern.matcher(orig);

        if(!matcher.find()) {
            throw new IllegalArgumentException(
                    String.format("Unable to replace find replacement for '%s' with the given pattern and group on action '%s'.", orig, action.getName()));
        }

        if(group == -1) {
            return orig.replaceFirst(regexReplacePattern, replacement);
        }

        int start = matcher.start(group);
        int end = matcher.end(group);

        StringBuilder buf = new StringBuilder(orig);
        buf.replace(start, end, replacement);

        return buf.toString();
    }

    public void waitForBuild(Action action, Job job, AbstractActionHandler handler) throws IOException, InterruptedException {
        if(MapUtils.isNotEmpty(action.getProperties())) {
            String waitTillDone = action.getProperties().getProperty(WAIT_TILL_DONE);

            if(waitTillDone != null && Boolean.parseBoolean(waitTillDone)) {
                String timeOut = action.getProperties().getProperty(TIME_OUT);
                String pollTime = action.getProperties().getProperty(POLL_TIME);

                long pollTimeMillis = JenkinsClient.DEFAULT_POLL_TIME_MILLIS;
                long timeOutMillis = JenkinsClient.DEFAULT_TIME_OUT_IN_MILLIS;

                if(StringUtils.isNotBlank(pollTime)) {
                    try {
                        pollTimeMillis = Long.parseLong(StringUtils.trim(pollTime));
                    } catch(NumberFormatException e) {
                        throw new IllegalArgumentException(String.format("Property '%s' for action '%s' is not a valid number.", POLL_TIME, action.getName()));
                    }
                }

                if(StringUtils.isNotBlank(timeOut)) {
                    try {
                        timeOutMillis = Long.parseLong(StringUtils.trim(timeOut));
                    } catch(NumberFormatException e) {
                        throw new IllegalArgumentException(String.format("Property '%s' for action '%s' is not a valid number.", TIME_OUT, action.getName()));
                    }
                }

                handler.logInfo(String.format(String.format("Waiting for build '%s' to complete...", job.getName())));
                handler.getClient().waitTillAllBuildsDone(job.getName(), pollTimeMillis, timeOutMillis);
            }
        }
    }
}
