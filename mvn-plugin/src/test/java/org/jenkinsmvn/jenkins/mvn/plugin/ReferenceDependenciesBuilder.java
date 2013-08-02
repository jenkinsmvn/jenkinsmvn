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

package org.jenkinsmvn.jenkins.mvn.plugin;

import org.jenkinsmvn.jenkins.api.JenkinsClient;
import org.jenkinsmvn.jenkins.api.model.JobDetails;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReferenceDependenciesBuilder {

    protected Map<String, List<String>> tree = new LinkedHashMap<String, List<String>>();

    private JenkinsClient client;

    public ReferenceDependenciesBuilder(JenkinsClient client) {
        this.client = client;
    }

    public String buildDependenciesReference(List<String> jobNames) throws IOException {
        for(String jobName : jobNames) {
            List<String> dependency = tree.get(jobName);

            if(dependency == null) {
                dependency = new LinkedList<String>();
                tree.put(jobName, dependency);
            }

            JobDetails details = client.getJobDetails(jobName);
            for(String upstream : details.getUpstreamProjectNames()) {
                if(jobNames.contains(upstream)) {
                    dependency.add(upstream);
                }
            }
        }

        StringBuilder buf = new StringBuilder();
        for(Map.Entry<String, List<String>> entry : tree.entrySet()) {
            if(CollectionUtils.isEmpty(entry.getValue())) {
                continue;
            }

            buf.append("\n    <reference>");
            buf.append("\n      <id>dependencies.").append(entry.getKey()).append("</id>");
            buf.append("\n      <jobs>");

            for(String dependency : entry.getValue()) {
                buf.append("\n        <job>").append(dependency).append("</job>");
            }

            buf.append("\n      </jobs>");
            buf.append("\n    </reference>");
        }

        for(Map.Entry<String, List<String>> entry : tree.entrySet()) {
            if(CollectionUtils.isEmpty(entry.getValue())) {
                continue;
            }

            buf.append("\n    <reference>");
            buf.append("\n      <id>dependencies.").append(entry.getKey()).append("-qa-branch").append("</id>");
            buf.append("\n      <refId>dependencies.").append(entry.getKey()).append("</refId>");
            buf.append("\n      <suffix>-qa-branch</suffix>");
            buf.append("\n    </reference>");
        }

        return buf.toString();
    }
}
