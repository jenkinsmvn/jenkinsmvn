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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.*;


/**
 * filter jobs
 */
public class FilterJobsActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "filter-jobs";

    private List<String> filters;

    public FilterJobsActionHandler() {
        super(TYPE);
    }

    @Override
    protected void initRelevantJobs(List<String> jobNames) {
        filters = Arrays.asList(HandlerUtils.INSTANCE.getRequiredProperty(action, FILTERS).split("[, ]"));
        logInfo(String.format("FILTERS: %s", String.valueOf(filters)));
    }

    @Override
    protected void doOnJob(Job job) throws IOException, InterruptedException, TransformerException, ParserConfigurationException {
        JobDetails details = client.getJobDetails(job.getName(), false);

        if(details.getBuildable() && filters.contains(FILTER_BUILDABLE)) {
            logInfo("included since job is enabled.");
            addJobResult(job.getName());

            return;
        }

        if(!details.getBuildable() && filters.contains(FILTER_NOT_BUILDABLE)) {
            logInfo("included since job is disabled.");
            addJobResult(job.getName());

            return;
        }

        if(details.getLastBuild() == null) {
            logInfo("excluded.");

            return;
        }

        BuildDetails buildDetails = client.getBuildDetails(details.getLastBuild());

        if(buildDetails.getResult().equals(JenkinsClient.RESULT_FAILURE) && filters.contains(FILTER_LAST_BUILD_FAILURE)) {
            logInfo(String.format("included since last build result is %s.", JenkinsClient.RESULT_FAILURE));
            addJobResult(job.getName());

            return;
        }

        if(buildDetails.getResult().equals(JenkinsClient.RESULT_SUCCESS) && filters.contains(FILTER_LAST_BUILD_SUCCESS)) {
            logInfo(String.format("included since last build result is %s.", JenkinsClient.RESULT_SUCCESS));
            addJobResult(job.getName());

            return;
        }

        if(buildDetails.getResult().equals(JenkinsClient.RESULT_UNSTABLE) && filters.contains(FILTER_LAST_BUILD_UNSTABLE)) {
            logInfo(String.format("included since last build result is %s.", JenkinsClient.RESULT_UNSTABLE));
            addJobResult(job.getName());

            return;
        }

        logInfo("excluded.");
    }
}
