package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.model.Job;

import java.io.IOException;

/**
 * Start disabling the job.
 */
public class EnableJobsActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "enable";

    public EnableJobsActionHandler() {
        super(TYPE, "enabled");
    }

    @Override
    protected void doOnJob(Job job) throws IOException {
        client.enable(job.getName());
    }
}
