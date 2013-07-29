package org.jenkinsmvn.jenkins.mvn.plugin.handler;


import org.jenkinsmvn.jenkins.api.model.Job;

import java.io.IOException;

public class TestUpstreamDependencyBuildJobActionHandler extends UpstreamDependencyBuildJobActionHandler {

    public static final String TYPE = "test-upstream-dependency-build";

    public TestUpstreamDependencyBuildJobActionHandler() {
        super(TYPE);
    }

    @Override
    protected void doOnJob(Job job) throws IOException, InterruptedException {
        //int random = (int) (1 + Math.random() * (6 - 1));

        int random = 1;

        logInfo(String.format("[thread=%s] Building %s job, random time %ds", Thread.currentThread().getName(), job.getName(), random));

        Thread.sleep(random * 1000);
    }
}
