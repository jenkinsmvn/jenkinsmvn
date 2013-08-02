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
import org.jenkinsmvn.jenkins.api.model.BuildDetails;
import org.jenkinsmvn.jenkins.api.model.Job;
import org.jenkinsmvn.jenkins.api.model.JobDetails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the build for the job
 */
public class VerifyBuildActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "verify-build";

    private List<FailedBuild> failures = new ArrayList<FailedBuild>();


    public VerifyBuildActionHandler() {
        super(TYPE);
    }

    @Override
    public void execute() throws IOException {
        super.execute();

        if(CollectionUtils.isNotEmpty(failures)) {
            StringBuilder buf = new StringBuilder(String.format("%d failed builds: ", failures.size()));
            String indent = StringUtils.repeat(" ", 10);

            for(FailedBuild failedBuild : failures) {
                buf.append("\n").append(indent).append(String.format("[%s] Build Number: %d", failedBuild.job.getName(), failedBuild.build.getNumber()));
                buf.append("\n").append(indent).append(String.format("[%s] URL: %s", failedBuild.job.getName(), String.valueOf(failedBuild.build.getUrl())));
            }

            logInfo(buf.toString());
            
            throw new IllegalArgumentException(String.format("%d failed builds found.", failures.size()));
        }
    }

    @Override
    protected void doOnJob(Job job) throws IOException, InterruptedException, TransformerException, ParserConfigurationException {
        JobDetails details = client.getJobDetails(job.getName(), false);

        if(details.getLastBuild() == null) {
            logInfo(String.format("Verified successful since no last build."));

            return;
        }

        BuildDetails build = client.getBuildDetails(details.getLastBuild());

        // wait for build
        if(build.getBuilding()) {
            waitForBuild(job);
            build = client.getBuildDetails(details.getLastBuild());
        }

        String message = String.format("last build '%d' with result '%s'.", build.getNumber(), build.getResult());
        if(JenkinsClient.RESULT_FAILURE.equals(build.getResult())) {
            message = String.format("Failed %s", message);
            logInfo(message);

            failures.add(new FailedBuild(details, build));
        } else {
            logInfo(String.format("Verified successful %s", message));
        }
    }

    private class FailedBuild {
        protected JobDetails job;

        protected BuildDetails build;

        private FailedBuild(JobDetails job, BuildDetails build) {
            this.build = build;
            this.job = job;
        }
    }
}
