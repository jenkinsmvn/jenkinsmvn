package org.jenkinsmvn.jenkins.mvn.plugin.handler;

import org.jenkinsmvn.jenkins.api.model.Job;

/**
 * Verify if jenkins jobs exists.
 */
public class VerifyJobsActionHandler extends AbstractForEachJobActionHandler {

    public static final String TYPE = "verify-exists";

    public VerifyJobsActionHandler() {
        super(TYPE, "verified");
    }

    @Override
    protected void doOnJob(Job job) {
        // do nothing.
    }
}
