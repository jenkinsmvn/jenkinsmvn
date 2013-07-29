package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.model.Job;

import java.io.IOException;

/**
 * Start disabling the job.
 */
public class DisableJobsActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "disable";

    public DisableJobsActionHandler() {
        super(TYPE, "disabled");
    }

    @Override
    protected void doOnJob(Job job) throws IOException, InterruptedException {
        // wait for builds currently running and queued to complete
        waitForBuild(job);

        client.disable(job.getName());
    }
}
