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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JobReferenceDependencyBuildJobActionHandler extends AbstractDependencyBuildJobActionHandler {

    public static final String TYPE = "reference-dependency-build";

    public JobReferenceDependencyBuildJobActionHandler() {
        super(TYPE);
    }

    public JobReferenceDependencyBuildJobActionHandler(String type) {
        super(type);
    }

    protected void buildTree(List<String> jobNames) throws IOException {
        logInfo("Generating dependency tree...");
        for(String jobName : jobNames) {
            List<String> dependency = tree.get(jobName);

            if(dependency == null) {
                dependency = new LinkedList<String>();
                tree.put(jobName, dependency);
            }

            List<String> jobReferences = getRefJobNames("dependencies." + jobName);
            for(String upstream : jobReferences) {
                if(jobNames.contains(upstream)) {
                    dependency.add(upstream);
                }
            }
        }

        StringBuilder buf = new StringBuilder("Generated Dependency Tree (references):");
        for(Map.Entry<String, List<String>> entry : tree.entrySet()) {
            buf.append("\n    ").append(entry.getKey()).append(": ").append(entry.getValue());
        }

        logInfo(buf.toString());
    }

}
