package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.JenkinsClient;
import org.jenkinsmvn.jenkins.api.model.Build;
import org.jenkinsmvn.jenkins.api.model.BuildDetails;
import org.jenkinsmvn.jenkins.api.model.Job;
import org.jenkinsmvn.jenkins.api.model.JobDetails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jenkinsmvn.jenkins.mvn.plugin.PropertyConstants.*;

/**
 * Start disabling the job.
 */
public class BuildJobsActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "build";

    public BuildJobsActionHandler() {
        super(TYPE);
    }

    public BuildJobsActionHandler(String type) {
        super(type);
    }

    @Override
    protected boolean isOnNoJobsContinue() {
        if(HandlerUtils.INSTANCE.isPropertyExist(action, FAIL_ON_NO_JOBS_TO_BUILD) &&
                HandlerUtils.INSTANCE.getRequiredBoolean(action, FAIL_ON_NO_JOBS_TO_BUILD)) {
            List<String> jobRefs = new ArrayList<String>();

            if(action.getJobListRef() != null) {
                jobRefs.add(action.getJobListRef());
            }
            if(CollectionUtils.isNotEmpty(action.getJobListRefs())) {
                jobRefs.addAll(action.getJobListRefs());
            }

            throw new IllegalStateException(String.format("%s Job reference with ids '%s' has no jobs.", logPrefix, jobRefs.toString()));
        }

        return true;
    }

    protected void doOnJob(Job job) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();

        if(HandlerUtils.INSTANCE.isPropertyExist(action, ENABLE_AND_BUILD) &&
                HandlerUtils.INSTANCE.getRequiredBoolean(action, ENABLE_AND_BUILD)) {
            JobDetails details = client.getJobDetails(job.getName(), false);

            // ensure that job is enabled before building.
            if(!details.getBuildable()) {
                client.enable(job.getName());
            }
        }

        String threadName = Thread.currentThread().getName();

        // wait for builds currently running and queued to complete
        waitForBuild(job);

        // only log if its more than 15 seconds
        if(System.currentTimeMillis() - startTime > LOG_TOLERANCE_MILLIS) {
            logInfo(String.format("[%s] Waited '%s' for all builds to complete.",
                    threadName,
                    HandlerUtils.INSTANCE.prettyPrintElapse(System.currentTimeMillis() - startTime))
            );
        }

        startTime = System.currentTimeMillis();
        Map<String, String> parameters = HandlerUtils.INSTANCE.createParameters(action, PARAMETER_PREFIX);

        if(MapUtils.isNotEmpty(parameters)) {
            logInfo(String.format("[%s] Build with parameters: %s", threadName, String.valueOf(parameters)));
        }

        logInfo(String.format("[%s] Started '%s' building.", threadName, job.getName()));
        client.build(job.getName(), true, parameters);

        // wait for the triggered build to complete
        waitForBuild(job);

        // fail on build failure
        if(HandlerUtils.INSTANCE.isPropertyExist(action, WAIT_TILL_DONE)) {
            if(HandlerUtils.INSTANCE.isPropertyExist(action, FAIL_ON_BUILD_FAILURE)) {
                JobDetails jobDetails = client.getJobDetails(job.getName(), false);
                Build build = jobDetails.getLastBuild();
                BuildDetails buildDetails = client.getBuildDetails(build);

                if(JenkinsClient.RESULT_FAILURE.equals(buildDetails.getResult())) {
                    throw new IllegalStateException(String.format("Build failed for job '%s'.", job.getName()));
                }

                logInfo(String.format("[%s] Build with number '%d' completed in '%s'.", threadName, buildDetails.getNumber(), HandlerUtils.INSTANCE.prettyPrintElapse(System.currentTimeMillis() - startTime)));
            } else {
                logInfo(String.format("[%s] Build completed in %s.", threadName, HandlerUtils.INSTANCE.prettyPrintElapse(System.currentTimeMillis() - startTime)));
            }
        } else {
            logInfo(String.format("[%s] Build asynchronous start.", threadName));
        }
    }
}
